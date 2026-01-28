package com.github.smolchanovsky.temporalplugin.analytics

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.util.UUID

/**
 * Application-level settings for anonymous analytics.
 *
 * Uses RoamingType.DISABLED to ensure settings are not synced
 * across JetBrains account (privacy consideration).
 */
@Service(Service.Level.APP)
@State(
    name = "TemporalAnalyticsSettings",
    storages = [Storage("temporal-analytics.xml", roamingType = RoamingType.DISABLED)]
)
class AnalyticsSettings : PersistentStateComponent<AnalyticsSettings.State> {

    data class State(
        var consentAsked: Boolean = false,
        var analyticsEnabled: Boolean = false,
        var installationId: String = UUID.randomUUID().toString()
    )

    private var myState = State()

    val consentAsked: Boolean
        get() = myState.consentAsked

    var analyticsEnabled: Boolean
        get() = myState.analyticsEnabled
        set(value) {
            val previousValue = myState.analyticsEnabled
            if (previousValue != value) {
                if (value) {
                    // Enabling: set first, then track
                    myState.analyticsEnabled = true
                    AnalyticsService.getInstance().track(SettingChangedEvent("analytics_consent", mapOf("enabled" to true)))
                } else {
                    // Disabling: track first, then set
                    AnalyticsService.getInstance().track(SettingChangedEvent("analytics_consent", mapOf("enabled" to false)))
                    myState.analyticsEnabled = false
                }
            }
        }

    val installationId: String
        get() = myState.installationId

    fun markConsentAsked() {
        myState.consentAsked = true
    }

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(): AnalyticsSettings =
            ApplicationManager.getApplication().getService(AnalyticsSettings::class.java)
    }
}
