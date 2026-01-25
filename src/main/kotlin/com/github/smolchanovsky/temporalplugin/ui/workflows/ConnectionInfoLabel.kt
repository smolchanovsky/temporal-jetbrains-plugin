package com.github.smolchanovsky.temporalplugin.ui.workflows

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.domain.Namespace
import com.github.smolchanovsky.temporalplugin.domain.Workflow
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateReader
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.SwingUtilities

class ConnectionInfoLabel(project: Project) : JPanel(BorderLayout()), Disposable {

    private val state: TemporalStateReader = project.service<TemporalState>()
    private val label = JBLabel()

    private val onStateChanged: (ConnectionState) -> Unit = { connectionState ->
        SwingUtilities.invokeLater {
            when (connectionState) {
                is ConnectionState.Disconnected -> showDisconnected()
                is ConnectionState.Connecting -> showConnecting()
                is ConnectionState.Connected -> showConnected(
                    connectionState.environment,
                    connectionState.namespace,
                    state.workflows.size
                )
                is ConnectionState.Refreshing -> showRefreshing()
            }
        }
    }

    private val onWorkflowsUpdated: (List<Workflow>) -> Unit = { workflows ->
        val connectionState = state.connectionState
        if (connectionState is ConnectionState.Connected) {
            SwingUtilities.invokeLater {
                showConnected(connectionState.environment, connectionState.namespace, workflows.size)
            }
        }
    }

    init {
        border = JBUI.Borders.empty(2, 0, 2, 8)
        add(label, BorderLayout.WEST)
        state.addConnectionStateListener(onStateChanged)
        state.addWorkflowsListener(onWorkflowsUpdated)
        onStateChanged(state.connectionState)
    }

    fun showDisconnected() = display(
        TextBundle.message("status.disconnected"),
        JBColor.GRAY
    )

    fun showConnecting() = display(
        TextBundle.message("status.connecting"),
        JBColor.ORANGE
    )

    fun showRefreshing() = display(
        TextBundle.message("status.refreshing"),
        JBColor.ORANGE
    )

    fun showConnected(env: Environment, ns: Namespace, count: Int) = display(
        TextBundle.message("status.connected", env.name, ns.name, count),
        JBColor.foreground()
    )

    private fun display(text: String, color: Color) {
        label.text = text
        label.foreground = color
    }

    override fun dispose() {
        state.removeConnectionStateListener(onStateChanged)
        state.removeWorkflowsListener(onWorkflowsUpdated)
    }
}
