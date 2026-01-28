package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.ui.analytics.base.TrackedAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project

class SettingsAction(
    private val project: Project
) : TrackedAction(
    analyticsName = "settings",
    text = TextBundle.message("action.settings"),
    description = TextBundle.message("action.settings.description"),
    icon = AllIcons.General.Settings
) {

    override fun doActionPerformed(e: AnActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, TextBundle.message("settings.title"))
    }
}
