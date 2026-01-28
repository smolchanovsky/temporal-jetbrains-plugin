package com.github.smolchanovsky.temporalplugin.ui.analytics.base

import com.github.smolchanovsky.temporalplugin.analytics.AnalyticsService
import com.github.smolchanovsky.temporalplugin.analytics.DialogCloseEvent
import com.github.smolchanovsky.temporalplugin.analytics.DialogOpenEvent
import com.intellij.openapi.options.BoundConfigurable

abstract class TrackedConfigurable(
    private val analyticsName: String,
    displayName: String
) : BoundConfigurable(displayName) {

    private val analytics = AnalyticsService.getInstance()
    private val openTime: Long = System.currentTimeMillis()

    init {
        analytics.track(DialogOpenEvent(analyticsName))
    }

    override fun disposeUIResources() {
        super.disposeUIResources()
        val durationMs = System.currentTimeMillis() - openTime
        analytics.track(DialogCloseEvent(analyticsName, durationMs))
    }
}
