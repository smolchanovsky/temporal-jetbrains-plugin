package com.github.smolchanovsky.temporalplugin.analytics

class StateChangeEvent(
    stateName: String,
    private val stateProperties: Map<String, Any>
) : AnalyticsEvent("state_$stateName") {
    override val properties: Map<String, Any>
        get() = stateProperties
}

class SettingChangedEvent(
    private val settingName: String,
    private val settingProperties: Map<String, Any>
) : AnalyticsEvent("setting_changed") {
    override val properties: Map<String, Any>
        get() = mapOf("setting" to settingName) + settingProperties
}
