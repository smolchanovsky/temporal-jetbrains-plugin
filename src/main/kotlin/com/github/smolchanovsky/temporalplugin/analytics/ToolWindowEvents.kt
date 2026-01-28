package com.github.smolchanovsky.temporalplugin.analytics

class ToolWindowOpenEvent(
    private val panel: String
) : AnalyticsEvent("tool_window_open") {
    override val properties: Map<String, Any>
        get() = mapOf("panel" to panel)
}

class ToolWindowCloseEvent(
    private val panel: String,
    private val durationMs: Long
) : AnalyticsEvent("tool_window_close") {
    override val properties: Map<String, Any>
        get() = mapOf(
            "panel" to panel,
            "duration_ms" to durationMs
        )
}
