package com.github.smolchanovsky.temporalplugin.ui.navigation

import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep

object WorkflowNavigationPopup {

    fun show(
        project: Project,
        title: String,
        items: List<WorkflowMatch>
    ) {
        if (items.isEmpty()) return

        val popupStep = object : BaseListPopupStep<WorkflowMatch>(title, items) {
            override fun getTextFor(value: WorkflowMatch): String {
                return formatDisplayText(value)
            }

            override fun onChosen(selectedValue: WorkflowMatch, finalChoice: Boolean): PopupStep<*>? {
                if (finalChoice) {
                    selectedValue.navigate(true)
                }
                return PopupStep.FINAL_CHOICE
            }

            override fun isSpeedSearchEnabled(): Boolean = true
        }

        JBPopupFactory.getInstance()
            .createListPopup(popupStep)
            .showInFocusCenter()
    }

    private fun formatDisplayText(match: WorkflowMatch): String = buildString {
        append(match.workflowType)
        if (!match.namespace.isNullOrEmpty()) {
            append(" (${match.namespace})")
        }
        append(" - ${match.fileName}")
        if (match.lineNumber > 0) {
            append(":${match.lineNumber}")
        }
    }
}
