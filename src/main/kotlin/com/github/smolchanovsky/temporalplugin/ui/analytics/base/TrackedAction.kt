package com.github.smolchanovsky.temporalplugin.ui.analytics.base

import com.github.smolchanovsky.temporalplugin.analytics.ActionEvent
import com.github.smolchanovsky.temporalplugin.analytics.AnalyticsService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import javax.swing.Icon

abstract class TrackedAction(
    private val analyticsName: String,
    text: String,
    description: String,
    icon: Icon
) : DumbAwareAction(text, description, icon) {

    private val analytics = AnalyticsService.getInstance()

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    final override fun actionPerformed(e: AnActionEvent) {
        analytics.track(ActionEvent("action_$analyticsName"))
        doActionPerformed(e)
    }

    protected abstract fun doActionPerformed(e: AnActionEvent)
}
