package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.usecase.DisconnectUseCase
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.launch

class DisconnectAction(
    project: Project
) : DumbAwareAction(
    TextBundle.message("action.disconnect"),
    TextBundle.message("action.disconnect.description"),
    AllIcons.Actions.Suspend
) {

    private val state = project.service<TemporalState>()
    private val mediator = project.service<TemporalMediator>().mediator

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        currentThreadCoroutineScope().launch {
            mediator.send(DisconnectUseCase)
        }
    }

    override fun update(e: AnActionEvent) {
        val connectionState = state.connectionState
        e.presentation.isEnabled = connectionState is ConnectionState.Connected || connectionState is ConnectionState.Connecting
    }
}
