package com.github.smolchanovsky.temporalplugin.analytics

class DialogOpenEvent(dialogName: String) : AnalyticsEvent("dialog_open_$dialogName")

class DialogCloseEvent(
    dialogName: String,
    private val durationMs: Long
) : AnalyticsEvent("dialog_close_$dialogName") {
    override val properties: Map<String, Any>
        get() = mapOf("duration_ms" to durationMs)
}

class DialogFieldChangedEvent(
    private val dialogName: String,
    private val fieldName: String
) : AnalyticsEvent("dialog_field_changed") {
    override val properties: Map<String, Any>
        get() = mapOf("dialog" to dialogName, "field" to fieldName)
}
