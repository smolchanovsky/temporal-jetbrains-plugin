package com.github.smolchanovsky.temporalplugin.ui.toolWindow

import com.github.smolchanovsky.temporalplugin.ui.details.WorkflowsRootPanel
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class TemporalToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = WorkflowsRootPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, null, false)
        content.setDisposer(panel)
        toolWindow.contentManager.addContent(content)
        Disposer.register(toolWindow.disposable, panel)
    }

    override fun shouldBeAvailable(project: Project) = true

    companion object {
        /** Must match id attribute in plugin.xml toolWindow element */
        const val TOOL_WINDOW_ID = "TemporalToolWindow"
    }
}
