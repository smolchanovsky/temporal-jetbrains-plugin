package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.ui.analytics.base.TrackedAction
import com.github.smolchanovsky.temporalplugin.usecase.DisconnectUseCase
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.Project
import kotlinx.coroutines.launch

class DisconnectAction(
    project: Project
) : TrackedAction(
    analyticsName = "disconnect",
    text = TextBundle.message("action.disconnect"),
    description = TextBundle.message("action.disconnect.description"),
    icon = AllIcons.Actions.ProfileRed
) {

    private val state = project.service<TemporalState>()
    private val mediator = project.service<TemporalMediator>().mediator

    override fun doActionPerformed(e: AnActionEvent) {
        currentThreadCoroutineScope().launch {
            mediator.send(DisconnectUseCase)
        }
    }

    override fun update(e: AnActionEvent) {
        val connectionState = state.connectionState
        val notDisconnected = connectionState !is ConnectionState.Disconnected
        e.presentation.isVisible = notDisconnected
        e.presentation.isEnabled = notDisconnected
    }
}
