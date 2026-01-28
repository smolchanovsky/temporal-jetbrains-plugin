package com.github.smolchanovsky.temporalplugin.ui.settings

import com.github.smolchanovsky.temporalplugin.utils.AddressUtils
import com.github.smolchanovsky.temporalplugin.analytics.AnalyticsService
import com.github.smolchanovsky.temporalplugin.analytics.SettingChangedEvent
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
        var refreshIntervalSeconds: Int = DEFAULT_REFRESH_INTERVAL,
        var temporalCliPath: String? = null,
        var serverAddress: String = DEFAULT_SERVER_ADDRESS,
        var webUiAddress: String = DEFAULT_WEB_UI_ADDRESS
    )

    private var state = State()
    private var initialized = false
    private val analytics = AnalyticsService.getInstance()

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
            if (state.refreshIntervalSeconds != value) {
                state.refreshIntervalSeconds = value
                analytics.track(SettingChangedEvent("refresh_interval", mapOf("value" to value)))
            }
        }

    override var temporalCliPath: String?
        get() = state.temporalCliPath
        set(value) {
            if (state.temporalCliPath != value) {
                state.temporalCliPath = value
                analytics.track(SettingChangedEvent("cli_path", mapOf("is_set" to (value != null))))
            }
        }

    var serverAddress: String
        get() = state.serverAddress
        set(value) {
            if (state.serverAddress != value) {
                state.serverAddress = value
                analytics.track(SettingChangedEvent("server_address", mapOf("type" to AddressUtils.classifyAddress(value, DEFAULT_SERVER_ADDRESS))))
            }
        }

    var webUiAddress: String
        get() = state.webUiAddress
        set(value) {
            if (state.webUiAddress != value) {
                state.webUiAddress = value
                analytics.track(SettingChangedEvent("web_ui_address", mapOf("type" to AddressUtils.classifyAddress(value, DEFAULT_WEB_UI_ADDRESS))))
            }
        }

    companion object {
        private const val DEFAULT_REFRESH_INTERVAL = 5
        private const val DEFAULT_SERVER_ADDRESS = "localhost:7233"
        private const val DEFAULT_WEB_UI_ADDRESS = "localhost:8233"

        fun getInstance(project: Project): TemporalSettings {
            return project.getService(TemporalSettings::class.java)
        }
    }
}
