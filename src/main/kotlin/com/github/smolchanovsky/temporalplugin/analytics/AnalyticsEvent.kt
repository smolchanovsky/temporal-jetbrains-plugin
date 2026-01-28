package com.github.smolchanovsky.temporalplugin.analytics

/**
 * Base class for all analytics events.
 */
sealed class AnalyticsEvent(val name: String) {
    open val properties: Map<String, Any> = emptyMap()
}
