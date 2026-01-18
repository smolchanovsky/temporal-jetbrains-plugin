package com.github.smolchanovsky.temporalplugin.ui.workflows

import com.github.smolchanovsky.temporalplugin.domain.Namespace
import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.cli.GetNamespacesQuery
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.ui.common.onFailureNotify
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import java.awt.Dimension
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.swing.JComponent
import javax.swing.SwingUtilities

class NamespaceSelector(
    private val project: Project,
    private val scope: CoroutineScope
) : ComboBoxAction(), DumbAware, Disposable {

    private val mediator = project.service<TemporalMediator>().mediator
    private val state = project.service<TemporalState>()
    private var namespaces: List<String> = emptyList()

    private val onConnectionStateChanged: (ConnectionState) -> Unit = { connectionState ->
        SwingUtilities.invokeLater {
            when (connectionState) {
                is ConnectionState.Connected -> load()
                is ConnectionState.Disconnected, is ConnectionState.Connecting -> {
                    namespaces = emptyList()
                }
                else -> {}
            }
        }
    }

    init {
        state.addConnectionStateListener(onConnectionStateChanged)
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val component = super.createCustomComponent(presentation, place)
        component.preferredSize = Dimension(120, component.preferredSize.height)
        component.maximumSize = Dimension(120, component.maximumSize.height)
        return component
    }

    override fun createPopupActionGroup(button: JComponent, dataContext: DataContext): DefaultActionGroup {
        val group = DefaultActionGroup()
        namespaces.forEach { ns ->
            group.add(SelectNamespaceAction(ns))
        }
        return group
    }

    override fun update(e: AnActionEvent) {
        val connectionState = state.connectionState
        e.presentation.isEnabled = connectionState is ConnectionState.Connected
        e.presentation.text = "NS: ${if (namespaces.isNotEmpty()) state.selectedNamespace.name else "-"}"
    }

    private fun load() {
        if (state.connectionState !is ConnectionState.Connected) return

        scope.launch {
            mediator.send(GetNamespacesQuery(state.selectedEnvironment))
                .onFailureNotify(project)
                .onSuccess { namespaceList ->
                    val names = namespaceList.map { it.name }
                    SwingUtilities.invokeLater {
                        namespaces = names
                        updateSelected(names)
                    }
                }
        }
    }

    private fun updateSelected(available: List<String>) {
        if (!available.contains(state.selectedNamespace.name)) {
            val name = available.firstOrNull() ?: state.selectedNamespace.name
            state.selectedNamespace = Namespace(name)
        }
    }

    override fun dispose() {
        state.removeConnectionStateListener(onConnectionStateChanged)
    }

    private inner class SelectNamespaceAction(
        private val namespace: String
    ) : AnAction(namespace) {

        override fun getActionUpdateThread() = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            state.selectedNamespace = Namespace(namespace)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = namespace != state.selectedNamespace.name
        }
    }
}
