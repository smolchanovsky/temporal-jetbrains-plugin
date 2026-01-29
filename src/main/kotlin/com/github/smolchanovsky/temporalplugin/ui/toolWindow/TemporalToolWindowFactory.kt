package com.github.smolchanovsky.temporalplugin.ui.toolWindow

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.analytics.AnalyticsService
import com.github.smolchanovsky.temporalplugin.analytics.ToolWindowCloseEvent
import com.github.smolchanovsky.temporalplugin.analytics.ToolWindowOpenEvent
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.github.smolchanovsky.temporalplugin.ui.details.WorkflowDetailsPanel
import com.github.smolchanovsky.temporalplugin.ui.settings.TemporalSettings
import com.github.smolchanovsky.temporalplugin.ui.workflows.WorkflowsPanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.swing.SwingUtilities

class TemporalToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val tabManager = ToolWindowTabManager(project, toolWindow)
        Disposer.register(toolWindow.disposable, tabManager)

        val tracker = ToolWindowVisibilityTracker(project, toolWindow)
        Disposer.register(toolWindow.disposable, tracker)
    }

    override fun shouldBeAvailable(project: Project) = true

    companion object {
        /** Must match id attribute in plugin.xml toolWindow element */
        const val TOOL_WINDOW_ID = "TemporalToolWindow"
    }
}

private class ToolWindowTabManager(
    private val project: Project,
    private val toolWindow: ToolWindow
) : Disposable {

    private val state = project.service<TemporalState>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var workflowsContent: Content
    private val detailsTabs = mutableMapOf<String, Content>()

    private val onViewStateChanged: (ViewState) -> Unit = { viewState ->
        SwingUtilities.invokeLater {
            when (viewState) {
                is ViewState.WorkflowList -> showWorkflowsList()
                is ViewState.WorkflowDetailsView -> showWorkflowDetails(viewState)
            }
        }
    }

    private val contentManagerListener = object : ContentManagerListener {
        override fun contentRemoved(event: ContentManagerEvent) {
            // Find and remove the closed tab from our map
            val closedRunId = detailsTabs.entries.find { it.value == event.content }?.key
            if (closedRunId != null) {
                detailsTabs.remove(closedRunId)
                // If the closed tab was the current view, go back to list
                val currentViewState = state.viewState
                if (currentViewState is ViewState.WorkflowDetailsView &&
                    currentViewState.workflow.runId == closedRunId) {
                    state.updateViewState(ViewState.WorkflowList)
                }
            }
        }
    }

    init {
        TemporalSettings.getInstance(project).ensureCliPathDetected()
        state.addViewStateListener(onViewStateChanged)
        toolWindow.contentManager.addContentManagerListener(contentManagerListener)
        createWorkflowsTab()
    }

    private fun createWorkflowsTab() {
        val panel = WorkflowsPanel(project, scope)
        val content = ContentFactory.getInstance().createContent(
            panel,
            TextBundle.message("toolwindow.tab.workflows"),
            false
        )
        content.setDisposer(panel)
        content.isCloseable = false
        toolWindow.contentManager.addContent(content)
        Disposer.register(this, panel)
        workflowsContent = content
    }

    private fun showWorkflowsList() {
        toolWindow.contentManager.setSelectedContent(workflowsContent)
    }

    private fun showWorkflowDetails(viewState: ViewState.WorkflowDetailsView) {
        val runId = viewState.workflow.runId

        // Check if tab for this workflow already exists
        val existingContent = detailsTabs[runId]
        if (existingContent != null) {
            // Tab exists, just select it
            toolWindow.contentManager.setSelectedContent(existingContent)
        } else {
            // Create new tab for this workflow
            val panel = WorkflowDetailsPanel(project, scope)
            val content = ContentFactory.getInstance().createContent(
                panel,
                viewState.workflow.id,
                false
            )
            content.setDisposer(panel)
            content.isCloseable = true
            toolWindow.contentManager.addContent(content)
            Disposer.register(this, panel)
            detailsTabs[runId] = content

            // Select the new tab
            toolWindow.contentManager.setSelectedContent(content)
        }
    }

    override fun dispose() {
        state.removeViewStateListener(onViewStateChanged)
        toolWindow.contentManager.removeContentManagerListener(contentManagerListener)
        detailsTabs.clear()
        scope.cancel()
    }
}

private class ToolWindowVisibilityTracker(
    private val project: Project,
    private val toolWindow: ToolWindow
) : ToolWindowManagerListener, com.intellij.openapi.Disposable {

    private val analytics = AnalyticsService.getInstance()
    private val state = project.service<TemporalState>()
    private var wasVisible = false
    private var openTime: Long = 0

    init {
        project.messageBus.connect(this).subscribe(
            ToolWindowManagerListener.TOPIC,
            this
        )

        if (toolWindow.isVisible) {
            onOpened()
        }
    }

    override fun stateChanged(toolWindowManager: com.intellij.openapi.wm.ToolWindowManager) {
        val isVisible = toolWindow.isVisible
        if (isVisible && !wasVisible) {
            onOpened()
        } else if (!isVisible && wasVisible) {
            onClosed()
        }
        wasVisible = isVisible
    }

    private fun onOpened() {
        wasVisible = true
        openTime = System.currentTimeMillis()
        analytics.track(ToolWindowOpenEvent(state.viewState::class.simpleName!!))
    }

    private fun onClosed() {
        wasVisible = false
        val duration = System.currentTimeMillis() - openTime
        analytics.track(ToolWindowCloseEvent(state.viewState::class.simpleName!!, duration))
    }

    override fun dispose() {
        if (wasVisible) {
            onClosed()
        }
    }
}
