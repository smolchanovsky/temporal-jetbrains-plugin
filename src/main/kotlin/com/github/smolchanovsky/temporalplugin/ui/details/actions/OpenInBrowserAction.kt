package com.github.smolchanovsky.temporalplugin.ui.details.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.ui.settings.TemporalSettings
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project

class OpenInBrowserAction(
    private val project: Project
) : DumbAwareAction(
    TextBundle.message("action.openInBrowser"),
    TextBundle.message("action.openInBrowser.description"),
    AllIcons.Ide.External_link_arrow
) {

    private val state = project.service<TemporalState>()
    private val settings = TemporalSettings.getInstance(project)

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val viewState = state.viewState
        val connectionState = state.connectionState

        if (viewState is ViewState.WorkflowDetailsView && connectionState is ConnectionState.Connected) {
            val webUiAddress = settings.webUiAddress.removePrefix("http://").removePrefix("https://")
            val namespace = connectionState.namespace.name
            val workflowId = viewState.workflow.id
            val runId = viewState.workflow.runId

            val url = "http://$webUiAddress/namespaces/$namespace/workflows/$workflowId/$runId"
            BrowserUtil.browse(url)
        }
    }

    override fun update(e: AnActionEvent) {
        val viewState = state.viewState
        e.presentation.isEnabled = viewState is ViewState.WorkflowDetailsView &&
                state.connectionState is ConnectionState.Connected
    }
}
