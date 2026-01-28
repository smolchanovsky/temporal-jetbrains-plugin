package com.github.smolchanovsky.temporalplugin.analytics

class ErrorEvent(
    private val errorType: String,
    private val cause: String? = null
) : AnalyticsEvent("error") {
    override val properties: Map<String, Any>
        get() = buildMap {
            put("error_type", errorType)
            cause?.let { put("cause", it) }
        }
}
