package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project

class SettingsAction(
    private val project: Project
) : DumbAwareAction(
    TextBundle.message("action.settings"),
    TextBundle.message("action.settings.description"),
    AllIcons.General.Settings
) {

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, TextBundle.message("settings.title"))
    }
}
