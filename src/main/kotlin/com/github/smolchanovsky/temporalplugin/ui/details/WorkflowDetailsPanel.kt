package com.github.smolchanovsky.temporalplugin.ui.details

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.github.smolchanovsky.temporalplugin.usecase.LoadWorkflowDetailsUseCase
import com.github.smolchanovsky.temporalplugin.ui.details.actions.OpenInBrowserAction
import com.github.smolchanovsky.temporalplugin.ui.details.actions.RefreshDetailsAction
import com.github.smolchanovsky.temporalplugin.ui.workflows.actions.CancelWorkflowActionGroup
import com.github.smolchanovsky.temporalplugin.ui.workflows.actions.GoToDefinitionAction
import com.github.smolchanovsky.temporalplugin.ui.workflows.actions.RerunWorkflowAction
import com.github.smolchanovsky.temporalplugin.ui.common.onFailureNotify
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBTabbedPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.SwingUtilities

class WorkflowDetailsPanel(
    private val project: Project,
    private val scope: CoroutineScope
) : JPanel(BorderLayout()), Disposable {

    private val state = project.service<TemporalState>()
    private val mediator = project.service<TemporalMediator>().mediator

    private val toolbar = WorkflowDetailsToolbar(
        refreshAction = RefreshDetailsAction(project, scope),
        rerunAction = RerunWorkflowAction(project, scope),
        cancelActionGroup = CancelWorkflowActionGroup(project, scope),
        goToDefinitionAction = GoToDefinitionAction(project),
        openInBrowserAction = OpenInBrowserAction(project)
    )

    private val tabbedPane = JBTabbedPane()
    private val overviewTab = WorkflowOverviewTab(project)
    private val dataTab = WorkflowInputResultTab(project)

    // Track which workflow is currently being loaded to prevent duplicate loads
    @Volatile
    private var currentlyLoadingWorkflowRunId: String? = null

    private val onViewStateChanged: (ViewState) -> Unit = { viewState ->
        SwingUtilities.invokeLater {
            when (viewState) {
                is ViewState.WorkflowDetailsView -> {
                    // Trigger loading if just opened and not already loading this workflow
                    if (viewState.isLoading &&
                        viewState.details == null &&
                        viewState.history == null &&
                        currentlyLoadingWorkflowRunId != viewState.workflow.runId) {
                        loadDetails(viewState)
                    }

                    // Clear loading flag when loading is complete
                    if (!viewState.isLoading) {
                        currentlyLoadingWorkflowRunId = null
                    }
                }
                is ViewState.WorkflowList -> {
                    currentlyLoadingWorkflowRunId = null
                }
            }
        }
    }

    init {
        Disposer.register(this, toolbar)
        Disposer.register(this, overviewTab)
        Disposer.register(this, dataTab)

        add(toolbar, BorderLayout.NORTH)

        tabbedPane.addTab(TextBundle.message("details.tab.overview"), overviewTab)
        tabbedPane.addTab(TextBundle.message("details.tab.inputResult"), dataTab)
        add(tabbedPane, BorderLayout.CENTER)

        state.addViewStateListener(onViewStateChanged)
    }

    private fun loadDetails(viewState: ViewState.WorkflowDetailsView) {
        currentlyLoadingWorkflowRunId = viewState.workflow.runId
        scope.launch {
            mediator.send(LoadWorkflowDetailsUseCase(viewState.workflow))
                .onFailureNotify(project)
        }
    }

    override fun dispose() {
        state.removeViewStateListener(onViewStateChanged)
    }
}
