package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.analytics.ActionEvent
import com.github.smolchanovsky.temporalplugin.analytics.AnalyticsService
import com.github.smolchanovsky.temporalplugin.domain.WorkflowStatus
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.ui.analytics.base.TrackedAction
import com.github.smolchanovsky.temporalplugin.ui.common.onFailureNotify
import com.github.smolchanovsky.temporalplugin.ui.workflows.dialog.TerminateWorkflowDialog
import com.github.smolchanovsky.temporalplugin.usecase.CancelWorkflowRequest
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.SplitButtonAction
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CancelWorkflowActionGroup(
    private val project: Project,
    scope: CoroutineScope
) : SplitButtonAction(
    DefaultActionGroup().apply {
        add(CancelWorkflowAction(project, scope))
        add(TerminateWorkflowAction(project, scope))
    }
), DumbAware {

    private val state = project.service<TemporalState>()

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        super.update(e)
        val connectionState = state.connectionState
        val connected = connectionState is ConnectionState.Connected ||
            connectionState is ConnectionState.Refreshing
        val isRunning = state.selectedWorkflow?.status == WorkflowStatus.RUNNING
        e.presentation.isEnabled = connected && isRunning
    }
}

private class CancelWorkflowAction(
    private val project: Project,
    private val scope: CoroutineScope
) : DumbAwareAction(
    TextBundle.message("action.cancel"),
    TextBundle.message("action.cancel.description"),
    AllIcons.Actions.Suspend
) {

    private val state = project.service<TemporalState>()
    private val mediator = project.service<TemporalMediator>().mediator
    private val analytics = AnalyticsService.getInstance()

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val workflow = state.selectedWorkflow ?: return

        analytics.track(ActionEvent("action_cancel_workflow"))

        scope.launch {
            mediator.send(
                CancelWorkflowRequest(
                    workflowId = workflow.id,
                    runId = workflow.runId
                )
            ).onFailureNotify(project)
        }
    }

    override fun update(e: AnActionEvent) {
        val connectionState = state.connectionState
        val connected = connectionState is ConnectionState.Connected ||
            connectionState is ConnectionState.Refreshing
        val isRunning = state.selectedWorkflow?.status == WorkflowStatus.RUNNING
        e.presentation.isEnabled = connected && isRunning
    }
}

private class TerminateWorkflowAction(
    private val project: Project,
    private val scope: CoroutineScope
) : TrackedAction(
    analyticsName = "terminate_workflow",
    text = TextBundle.message("action.terminate"),
    description = TextBundle.message("action.terminate.description"),
    icon = AllIcons.Debugger.KillProcess
) {

    private val state = project.service<TemporalState>()

    override fun doActionPerformed(e: AnActionEvent) {
        val workflow = state.selectedWorkflow ?: return

        invokeLater {
            TerminateWorkflowDialog(project, scope, workflow).show()
        }
    }

    override fun update(e: AnActionEvent) {
        val connectionState = state.connectionState
        val connected = connectionState is ConnectionState.Connected ||
            connectionState is ConnectionState.Refreshing
        val isRunning = state.selectedWorkflow?.status == WorkflowStatus.RUNNING
        e.presentation.isEnabled = connected && isRunning
    }
}
