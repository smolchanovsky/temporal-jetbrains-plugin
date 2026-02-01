package com.github.smolchanovsky.temporalplugin.ui.toolWindow

import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.ui.onboarding.ShowOnboardingToolWindow
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class ToolWindowStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val mediator = project.service<TemporalMediator>().mediator
        mediator.send(ShowOnboardingToolWindow(project))
    }
}
