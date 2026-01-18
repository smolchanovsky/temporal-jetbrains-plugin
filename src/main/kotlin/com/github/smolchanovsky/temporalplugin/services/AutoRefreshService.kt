package com.github.smolchanovsky.temporalplugin.services

import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.ui.settings.TemporalSettings
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateReader
import com.github.smolchanovsky.temporalplugin.ui.toolWindow.TemporalToolWindowFactory
import com.github.smolchanovsky.temporalplugin.ui.common.onFailureNotify
import com.github.smolchanovsky.temporalplugin.usecase.RefreshUseCase
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

@Service(Service.Level.PROJECT)
class AutoRefreshService(
    private val project: Project
) : Disposable {

    private val state: TemporalStateReader = project.service<TemporalState>()
    private val settings = project.service<TemporalSettings>()
    private val mediator = project.service<TemporalMediator>().mediator
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var refreshTimer: Timer? = null

    private val onStateChanged: (ConnectionState) -> Unit = { connectionState ->
        when (connectionState) {
            is ConnectionState.Connected -> startTimer()
            else -> stopTimer()
        }
    }

    init {
        state.addConnectionStateListener(onStateChanged)
    }

    private fun startTimer() {
        stopTimer()
        val intervalMs = settings.refreshIntervalSeconds * 1000L
        refreshTimer = Timer("TemporalAutoRefresh", true).apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() = refresh()
            }, intervalMs, intervalMs)
        }
    }

    private fun stopTimer() {
        refreshTimer?.cancel()
        refreshTimer = null
    }

    private fun refresh() {
        if (state.connectionState is ConnectionState.Connected && isToolWindowVisible()) {
            scope.launch {
                mediator.send(RefreshUseCase).onFailureNotify(project)
            }
        }
    }

    private fun isToolWindowVisible(): Boolean {
        val toolWindow = ToolWindowManager.Companion.getInstance(project).getToolWindow(TemporalToolWindowFactory.Companion.TOOL_WINDOW_ID)
        return toolWindow?.isVisible == true
    }

    override fun dispose() {
        stopTimer()
        scope.cancel()
        state.removeConnectionStateListener(onStateChanged)
    }
}