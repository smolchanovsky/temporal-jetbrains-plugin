package com.github.smolchanovsky.temporalplugin.ui.settings

import com.github.smolchanovsky.temporalplugin.cli.TemporalCliConfig
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "TemporalSettings",
    storages = [Storage("temporal.xml")]
)
class TemporalSettings : PersistentStateComponent<TemporalSettings.State>, TemporalCliConfig {

    data class State(
        var refreshIntervalSeconds: Int = 5,
        var temporalCliPath: String? = null,
        var serverAddress: String = "localhost:7233",
        var webUiAddress: String = "localhost:8233"
    )

    private var state = State()
    private var initialized = false

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    fun ensureCliPathDetected() {
        if (initialized) return
        initialized = true

        if (state.temporalCliPath == null) {
            state.temporalCliPath = TemporalCliDetector.detect()
        }
    }

    var refreshIntervalSeconds: Int
        get() = state.refreshIntervalSeconds
        set(value) {
            state.refreshIntervalSeconds = value
        }

    override var temporalCliPath: String?
        get() = state.temporalCliPath
        set(value) {
            state.temporalCliPath = value
        }

    var serverAddress: String
        get() = state.serverAddress
        set(value) {
            state.serverAddress = value
        }

    var webUiAddress: String
        get() = state.webUiAddress
        set(value) {
            state.webUiAddress = value
        }

    companion object {
        fun getInstance(project: Project): TemporalSettings {
            return project.getService(TemporalSettings::class.java)
        }
    }
}
