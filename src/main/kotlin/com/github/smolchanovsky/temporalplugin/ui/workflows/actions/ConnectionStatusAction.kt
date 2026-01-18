package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateReader
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project

class ConnectionStatusAction(
    project: Project
) : DumbAwareAction() {

    private val state: TemporalStateReader = project.service<TemporalState>()

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        // No action - status is display only
    }

    override fun update(e: AnActionEvent) {
        val icon = when (state.connectionState) {
            is ConnectionState.Disconnected -> {
                e.presentation.text = TextBundle.message("action.status.disconnected")
                e.presentation.description = TextBundle.message("action.status.disconnected.description")
                AllIcons.RunConfigurations.TestIgnored
            }
            is ConnectionState.Connecting -> {
                e.presentation.text = TextBundle.message("action.status.connecting")
                e.presentation.description = TextBundle.message("action.status.connecting.description")
                AllIcons.Process.Step_1
            }
            is ConnectionState.Connected -> {
                e.presentation.text = TextBundle.message("action.status.connected")
                e.presentation.description = TextBundle.message("action.status.connected.description")
                AllIcons.Status.Success
            }
            is ConnectionState.Refreshing -> {
                e.presentation.text = TextBundle.message("action.status.connected")
                e.presentation.description = TextBundle.message("action.status.connected.description")
                AllIcons.Process.Step_1
            }
        }
        e.presentation.icon = icon
        e.presentation.disabledIcon = icon
        e.presentation.isEnabled = false
    }
}
