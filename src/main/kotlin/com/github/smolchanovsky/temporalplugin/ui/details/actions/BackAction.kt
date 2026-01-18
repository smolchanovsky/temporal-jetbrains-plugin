package com.github.smolchanovsky.temporalplugin.ui.details.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project

class BackAction(
    private val project: Project
) : DumbAwareAction(
    TextBundle.message("action.back"),
    TextBundle.message("action.back.description"),
    AllIcons.Actions.Back
) {

    private val state = project.service<TemporalState>()

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        state.updateViewState(ViewState.WorkflowList)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = state.viewState is ViewState.WorkflowDetailsView
    }
}
