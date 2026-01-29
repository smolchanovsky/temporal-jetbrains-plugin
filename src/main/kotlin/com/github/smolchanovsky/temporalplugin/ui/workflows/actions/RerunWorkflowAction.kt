package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.ui.analytics.base.TrackedAction
import com.github.smolchanovsky.temporalplugin.ui.common.onFailureNotify
import com.github.smolchanovsky.temporalplugin.ui.workflows.dialog.RerunWorkflowDialog
import com.github.smolchanovsky.temporalplugin.usecase.GenerateWorkflowDataRequest
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RerunWorkflowAction(
    private val project: Project,
    private val scope: CoroutineScope
) : TrackedAction(
    analyticsName = "rerun_workflow",
    text = TextBundle.message("action.rerun"),
    description = TextBundle.message("action.rerun.description"),
    icon = AllIcons.Actions.Rerun
) {

    private val state = project.service<TemporalState>()
    private val mediator = project.service<TemporalMediator>().mediator

    override fun doActionPerformed(e: AnActionEvent) {
        val workflow = state.selectedWorkflow ?: return

        scope.launch {
            val result = mediator.send(GenerateWorkflowDataRequest(workflow))

            if (result.isFailure) {
                result.onFailureNotify(project)
                return@launch
            }

            val data = result.getOrThrow()

            invokeLater {
                RerunWorkflowDialog(project, scope, data).show()
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val connectionState = state.connectionState
        val connected = connectionState is ConnectionState.Connected ||
            connectionState is ConnectionState.Refreshing
        val hasSelection = state.selectedWorkflow != null
        e.presentation.isEnabled = connected && hasSelection
    }
}
