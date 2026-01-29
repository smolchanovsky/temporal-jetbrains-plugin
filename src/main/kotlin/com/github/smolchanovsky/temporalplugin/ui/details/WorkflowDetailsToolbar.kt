package com.github.smolchanovsky.temporalplugin.ui.details

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import java.awt.BorderLayout
import javax.swing.JPanel

class WorkflowDetailsToolbar(
    refreshAction: AnAction,
    rerunAction: AnAction,
    cancelActionGroup: AnAction,
    goToDefinitionAction: AnAction,
    openInBrowserAction: AnAction
) : JPanel(BorderLayout()), Disposable {

    init {
        val actionGroup = DefaultActionGroup().apply {
            add(refreshAction)
            addSeparator()
            add(rerunAction)
            add(cancelActionGroup)
            addSeparator()
            add(openInBrowserAction)
            add(goToDefinitionAction)
        }

        val toolbar = ActionManager.getInstance()
            .createActionToolbar("WorkflowDetailsToolbar", actionGroup, true)
        toolbar.targetComponent = this

        add(toolbar.component, BorderLayout.WEST)
    }

    override fun dispose() {
        // No specific cleanup needed
    }
}
