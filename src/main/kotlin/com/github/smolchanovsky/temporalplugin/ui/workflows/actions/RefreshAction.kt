package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateReader
import com.github.smolchanovsky.temporalplugin.usecase.RefreshUseCase
import com.github.smolchanovsky.temporalplugin.ui.common.onFailureNotify
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.launch

class RefreshAction(
    private val project: Project
) : DumbAwareAction(
    TextBundle.message("action.refresh"),
    TextBundle.message("action.refresh.description"),
    AllIcons.Actions.Refresh
) {

    private val state: TemporalStateReader = project.service<TemporalState>()
    private val mediator = project.service<TemporalMediator>().mediator

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        currentThreadCoroutineScope().launch {
            mediator.send(RefreshUseCase)
                .onFailureNotify(project)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = state.connectionState is ConnectionState.Connected
    }
}
