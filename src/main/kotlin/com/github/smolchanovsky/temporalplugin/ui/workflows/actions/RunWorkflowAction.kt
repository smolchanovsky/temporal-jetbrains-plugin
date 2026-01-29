package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.ui.analytics.base.TrackedAction
import com.github.smolchanovsky.temporalplugin.ui.workflows.dialog.RerunWorkflowDialog
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

class RunWorkflowAction(
    private val project: Project,
    private val scope: CoroutineScope
) : TrackedAction(
    analyticsName = "run_workflow",
    text = TextBundle.message("action.run"),
    description = TextBundle.message("action.run.description"),
    icon = AllIcons.Actions.Execute
) {

    private val state = project.service<TemporalState>()

    override fun doActionPerformed(e: AnActionEvent) {
        invokeLater {
            RerunWorkflowDialog(project, scope).show()
        }
    }

    override fun update(e: AnActionEvent) {
        val connectionState = state.connectionState
        val connected = connectionState is ConnectionState.Connected ||
            connectionState is ConnectionState.Refreshing
        e.presentation.isEnabled = connected
    }
}
