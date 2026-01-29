package com.github.smolchanovsky.temporalplugin.ui.workflows

import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateReader
import com.github.smolchanovsky.temporalplugin.usecase.RefreshUseCase
import com.github.smolchanovsky.temporalplugin.ui.common.onFailureNotify
import com.github.smolchanovsky.temporalplugin.ui.workflows.actions.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.SwingUtilities

class WorkflowsToolbar(
    private val project: Project,
    private val scope: CoroutineScope
) : JPanel(BorderLayout()), Disposable {

    private val state: TemporalStateReader = project.service<TemporalState>()
    private val mediator = project.service<TemporalMediator>().mediator

    private val envSelector: EnvironmentSelector
    private val nsSelector: NamespaceSelector
    private val leftToolbar: ActionToolbar
    private val rightToolbar: ActionToolbar

    private val onEnvironmentChanged: () -> Unit = { refreshIfConnected() }
    private val onNamespaceChanged: () -> Unit = { refreshIfConnected() }
    private val onConnectionStateChanged: (ConnectionState) -> Unit = {
        SwingUtilities.invokeLater {
            leftToolbar.updateActionsAsync()
            rightToolbar.updateActionsAsync()
        }
    }
    private val onSelectedWorkflowChanged: () -> Unit = {
        SwingUtilities.invokeLater {
            leftToolbar.updateActionsAsync()
        }
    }

    init {
        state.addEnvironmentListener(onEnvironmentChanged)
        state.addNamespaceListener(onNamespaceChanged)
        state.addConnectionStateListener(onConnectionStateChanged)
        state.addSelectedWorkflowListener(onSelectedWorkflowChanged)

        envSelector = EnvironmentSelector(project, scope)
        nsSelector = NamespaceSelector(project, scope)

        Disposer.register(this, nsSelector)

        val leftActionGroup = DefaultActionGroup().apply {
            add(ConnectAction(project))
            add(DisconnectAction(project))
            add(RefreshAction(project))
            addSeparator()
            add(RerunWorkflowAction(project, scope))
            add(CancelWorkflowActionGroup(project, scope))
            add(GoToDefinitionAction(project))
            addSeparator()
            add(SettingsAction(project))
        }

        val rightActionGroup = DefaultActionGroup().apply {
            add(envSelector)
            add(nsSelector)
        }

        leftToolbar = ActionManager.getInstance().createActionToolbar(
            ActionPlaces.TOOLWINDOW_CONTENT,
            leftActionGroup,
            true
        )
        leftToolbar.targetComponent = this

        rightToolbar = ActionManager.getInstance().createActionToolbar(
            ActionPlaces.TOOLWINDOW_CONTENT,
            rightActionGroup,
            true
        )
        rightToolbar.targetComponent = this

        add(leftToolbar.component, BorderLayout.WEST)
        add(rightToolbar.component, BorderLayout.EAST)
    }

    private fun refreshIfConnected() {
        if (state.connectionState is ConnectionState.Connected) {
            scope.launch {
                mediator.send(RefreshUseCase).onFailureNotify(project)
            }
        }
    }

    fun load() {
        envSelector.load()
    }

    override fun dispose() {
        state.removeEnvironmentListener(onEnvironmentChanged)
        state.removeNamespaceListener(onNamespaceChanged)
        state.removeConnectionStateListener(onConnectionStateChanged)
        state.removeSelectedWorkflowListener(onSelectedWorkflowChanged)
    }
}
