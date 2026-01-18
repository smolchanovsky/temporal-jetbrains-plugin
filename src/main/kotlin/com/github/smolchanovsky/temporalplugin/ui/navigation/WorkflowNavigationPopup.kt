package com.github.smolchanovsky.temporalplugin.ui.navigation

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep

object WorkflowNavigationPopup {

    fun show(
        project: Project,
        title: String,
        items: List<WorkflowNavigationItem>
    ) {
        if (items.isEmpty()) return

        val popupStep = object : com.intellij.openapi.ui.popup.util.BaseListPopupStep<WorkflowNavigationItem>(title, items) {
            override fun getTextFor(value: WorkflowNavigationItem): String {
                return value.displayPresentation
            }

            override fun onChosen(selectedValue: WorkflowNavigationItem, finalChoice: Boolean): PopupStep<*>? {
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
}
