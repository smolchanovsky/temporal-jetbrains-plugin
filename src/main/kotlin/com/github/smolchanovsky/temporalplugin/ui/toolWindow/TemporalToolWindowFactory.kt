package com.github.smolchanovsky.temporalplugin.ui.toolWindow

import com.github.smolchanovsky.temporalplugin.analytics.AnalyticsService
import com.github.smolchanovsky.temporalplugin.analytics.ToolWindowCloseEvent
import com.github.smolchanovsky.temporalplugin.analytics.ToolWindowOpenEvent
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.github.smolchanovsky.temporalplugin.ui.details.WorkflowsRootPanel
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.content.ContentFactory

class TemporalToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = WorkflowsRootPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, null, false)
        content.setDisposer(panel)
        toolWindow.contentManager.addContent(content)
        Disposer.register(toolWindow.disposable, panel)

        val tracker = ToolWindowVisibilityTracker(project, toolWindow)
        Disposer.register(toolWindow.disposable, tracker)
    }

    override fun shouldBeAvailable(project: Project) = true

    companion object {
        /** Must match id attribute in plugin.xml toolWindow element */
        const val TOOL_WINDOW_ID = "TemporalToolWindow"
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
