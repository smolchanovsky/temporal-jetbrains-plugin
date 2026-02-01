package com.github.smolchanovsky.temporalplugin.ui.onboarding

import com.github.smolchanovsky.temporalplugin.ui.toolWindow.TemporalToolWindowFactory
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler

data class ShowOnboardingToolWindow(val project: Project) : Request.Unit

class ShowOnboardingToolWindowHandler : RequestHandler.Unit<ShowOnboardingToolWindow> {

    companion object {
        private const val COMPLETED_KEY = "temporal.onboarding.showToolWindow.completed"
    }

    override suspend fun handle(request: ShowOnboardingToolWindow) {
        if (isCompleted()) return

        markCompleted()
        showToolWindow(request.project)
    }

    private fun isCompleted(): Boolean {
        return PropertiesComponent.getInstance().getBoolean(COMPLETED_KEY, false)
    }

    private fun markCompleted() {
        PropertiesComponent.getInstance().setValue(COMPLETED_KEY, true)
    }

    private fun showToolWindow(project: Project) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        toolWindowManager.invokeLater {
            toolWindowManager.getToolWindow(TemporalToolWindowFactory.TOOL_WINDOW_ID)?.show()
        }
    }
}
