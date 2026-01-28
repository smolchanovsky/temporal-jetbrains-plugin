package com.github.smolchanovsky.temporalplugin.ui.analytics.base

import com.github.smolchanovsky.temporalplugin.analytics.AnalyticsService
import com.github.smolchanovsky.temporalplugin.analytics.DialogCloseEvent
import com.github.smolchanovsky.temporalplugin.analytics.DialogOpenEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper

abstract class TrackedDialog(
    private val analyticsName: String,
    project: Project
) : DialogWrapper(project) {

    protected val fieldTracker: DialogFieldTracker = DialogFieldTracker(analyticsName)

    private val analytics = AnalyticsService.getInstance()
    private var openTime: Long = 0

    protected fun trackOpen() {
        openTime = System.currentTimeMillis()
        analytics.track(DialogOpenEvent(analyticsName))
    }

    override fun dispose() {
        val durationMs = System.currentTimeMillis() - openTime
        analytics.track(DialogCloseEvent(analyticsName, durationMs))
        super.dispose()
    }
}
