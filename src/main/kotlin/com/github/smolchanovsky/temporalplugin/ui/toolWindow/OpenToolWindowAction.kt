package com.github.smolchanovsky.temporalplugin.ui.toolWindow

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.wm.ToolWindowManager

class OpenToolWindowAction : DumbAwareAction(
    TextBundle.message("action.openToolWindow"),
    TextBundle.message("action.openToolWindow.description"),
    null
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow(TemporalToolWindowFactory.TOOL_WINDOW_ID)
        toolWindow?.show()
    }
}
