package com.github.smolchanovsky.temporalplugin.ui.details.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.github.smolchanovsky.temporalplugin.usecase.LoadWorkflowDetailsUseCase
import com.github.smolchanovsky.temporalplugin.ui.common.onFailureNotify
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RefreshDetailsAction(
    private val project: Project,
    private val scope: CoroutineScope
) : DumbAwareAction(
    TextBundle.message("action.refresh"),
    TextBundle.message("action.refresh.description"),
    AllIcons.Actions.Refresh
) {

    private val state = project.service<TemporalState>()
    private val mediator = project.service<TemporalMediator>().mediator

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val viewState = state.viewState
        if (viewState is ViewState.WorkflowDetailsView && !viewState.isLoading) {
            scope.launch {
                mediator.send(LoadWorkflowDetailsUseCase(viewState.workflow))
                    .onFailureNotify(project)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val viewState = state.viewState
        e.presentation.isEnabled = viewState is ViewState.WorkflowDetailsView && !viewState.isLoading
    }
}
