package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.github.smolchanovsky.temporalplugin.ui.analytics.base.TrackedAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class OpenDetailsAction(
    project: Project
) : TrackedAction(
    analyticsName = "open_details",
    text = TextBundle.message("action.openDetails"),
    description = TextBundle.message("action.openDetails.description"),
    icon = AllIcons.Actions.Preview
) {

    private val state = project.service<TemporalState>()

    override fun doActionPerformed(e: AnActionEvent) {
        val workflow = state.selectedWorkflow ?: return
        state.updateViewState(ViewState.WorkflowDetailsView(workflow, isLoading = true))
    }

    override fun update(e: AnActionEvent) {
        val connectionState = state.connectionState
        val connected = connectionState is ConnectionState.Connected ||
            connectionState is ConnectionState.Refreshing
        val hasSelection = state.selectedWorkflow != null
        e.presentation.isEnabled = connected && hasSelection
    }
}
