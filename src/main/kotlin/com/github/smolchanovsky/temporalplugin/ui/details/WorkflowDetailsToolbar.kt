package com.github.smolchanovsky.temporalplugin.ui.details

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import javax.swing.JPanel

class WorkflowDetailsToolbar(
    backAction: AnAction,
    refreshAction: AnAction,
    runSimilarAction: AnAction,
    goToDefinitionAction: AnAction,
    openInBrowserAction: AnAction
) : JPanel(BorderLayout()), Disposable {

    private val titleLabel = JBLabel()

    init {
        val actionGroup = DefaultActionGroup().apply {
            add(backAction)
            addSeparator()
            add(refreshAction)
            add(runSimilarAction)
            add(goToDefinitionAction)
            add(openInBrowserAction)
        }

        val toolbar = ActionManager.getInstance()
            .createActionToolbar("WorkflowDetailsToolbar", actionGroup, true)
        toolbar.targetComponent = this

        add(toolbar.component, BorderLayout.WEST)
        add(titleLabel, BorderLayout.CENTER)
    }

    fun updateTitle(workflowId: String) {
        titleLabel.text = if (workflowId.isNotEmpty()) TextBundle.message("details.workflowTitle", workflowId) else ""
    }

    override fun dispose() {
        // No specific cleanup needed
    }
}
