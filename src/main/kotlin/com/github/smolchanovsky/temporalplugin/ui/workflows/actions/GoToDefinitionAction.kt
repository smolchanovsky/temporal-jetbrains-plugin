package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.ui.analytics.base.TrackedAction
import com.github.smolchanovsky.temporalplugin.ui.navigation.WorkflowNavigationService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class GoToDefinitionAction(
    project: Project
) : TrackedAction(
    analyticsName = "go_to_definition",
    text = TextBundle.message("action.goToDefinition"),
    description = TextBundle.message("action.goToDefinition.description"),
    icon = AllIcons.Json.Object
) {

    private val state = project.service<TemporalState>()
    private val nav = project.service<WorkflowNavigationService>()

    override fun doActionPerformed(e: AnActionEvent) {
        state.selectedWorkflow?.let { nav.navigateToWorkflowDefinition(it.type) }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = state.selectedWorkflow != null
    }
}
