package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateReader
import com.github.smolchanovsky.temporalplugin.ui.analytics.base.TrackedAction
import com.github.smolchanovsky.temporalplugin.ui.common.onFailureNotify
import com.github.smolchanovsky.temporalplugin.usecase.RefreshUseCase
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.Project
import kotlinx.coroutines.launch

class RefreshAction(
    private val project: Project
) : TrackedAction(
    analyticsName = "refresh",
    text = TextBundle.message("action.refresh"),
    description = TextBundle.message("action.refresh.description"),
    icon = AllIcons.Actions.Refresh
) {

    private val state: TemporalStateReader = project.service<TemporalState>()
    private val mediator = project.service<TemporalMediator>().mediator

    override fun doActionPerformed(e: AnActionEvent) {
        currentThreadCoroutineScope().launch {
            mediator.send(RefreshUseCase)
                .onFailureNotify(project)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = state.connectionState is ConnectionState.Connected
    }
}
