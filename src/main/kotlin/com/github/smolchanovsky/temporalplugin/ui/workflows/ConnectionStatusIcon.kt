package com.github.smolchanovsky.temporalplugin.ui.workflows

import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateReader
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.IconUtil
import com.intellij.util.ui.JBUI
import javax.swing.SwingUtilities

class ConnectionStatusIcon(project: Project) : JBLabel(), Disposable {

    private val state: TemporalStateReader = project.service<TemporalState>()

    private val onStateChanged: (ConnectionState) -> Unit = { connectionState ->
        SwingUtilities.invokeLater {
            icon = when (connectionState) {
                is ConnectionState.Disconnected -> AllIcons.RunConfigurations.TestIgnored
                is ConnectionState.Connecting -> AllIcons.Process.Step_1
                is ConnectionState.Connected -> AllIcons.RunConfigurations.TestPassed
                is ConnectionState.Refreshing -> AllIcons.Process.Step_1
            }.let { IconUtil.scale(it, null, 0.75f) }
        }
    }

    init {
        border = JBUI.Borders.empty(2, 8, 2, 4)
        state.addConnectionStateListener(onStateChanged)
        onStateChanged(state.connectionState)
    }

    override fun dispose() {
        state.removeConnectionStateListener(onStateChanged)
    }
}
