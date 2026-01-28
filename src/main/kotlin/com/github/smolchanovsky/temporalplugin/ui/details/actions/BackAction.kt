package com.github.smolchanovsky.temporalplugin.ui.details.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.github.smolchanovsky.temporalplugin.ui.analytics.base.TrackedAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class BackAction(
    private val project: Project
) : TrackedAction(
    analyticsName = "back",
    text = TextBundle.message("action.back"),
    description = TextBundle.message("action.back.description"),
    icon = AllIcons.Actions.Back
) {

    private val state = project.service<TemporalState>()

    override fun doActionPerformed(e: AnActionEvent) {
        state.updateViewState(ViewState.WorkflowList)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = state.viewState is ViewState.WorkflowDetailsView
    }
}
