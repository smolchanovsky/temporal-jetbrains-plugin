package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.ui.navigation.WorkflowNavigationService
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project

class GoToDefinitionAction(
    project: Project
) : DumbAwareAction(
    TextBundle.message("action.goToDefinition"),
    TextBundle.message("action.goToDefinition.description"),
    AllIcons.Json.Object
) {

    private val state = project.service<TemporalState>()
    private val nav = project.service<WorkflowNavigationService>()

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        state.selectedWorkflow?.let { nav.navigateToWorkflowDefinition(it.type) }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = state.selectedWorkflow != null
    }
}
