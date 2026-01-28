package com.github.smolchanovsky.temporalplugin.ui.workflows.actions

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.ui.analytics.base.TrackedAction
import com.github.smolchanovsky.temporalplugin.ui.navigation.IdeLanguageSupport
import com.github.smolchanovsky.temporalplugin.ui.navigation.WorkflowNavigationService
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.ui.GotItTooltip
import java.net.URI
import javax.swing.JComponent

class GoToDefinitionAction(
    private val project: Project
) : TrackedAction(
    analyticsName = "go_to_definition",
    text = TextBundle.message("action.goToDefinition"),
    description = TextBundle.message("action.goToDefinition.description"),
    icon = AllIcons.Json.Object
) {

    private val state = project.service<TemporalState>()
    private val nav = project.service<WorkflowNavigationService>()

    companion object {
        private const val TOOLTIP_ID = "temporal.navigation.unsupported.language"
        private const val PROPERTY_KEY = "got.it.tooltip.$TOOLTIP_ID"
    }

    override fun doActionPerformed(e: AnActionEvent) {
        if (!nav.hasFinders()) {
            showUnsupportedLanguageTooltip(e)
            return
        }
        state.selectedWorkflow?.let { nav.navigateToWorkflowDefinition(it.type) }
    }

    override fun update(e: AnActionEvent) {
        val hasFinders = nav.hasFinders()
        val hasSelectedWorkflow = state.selectedWorkflow != null

        if (!hasFinders && isTooltipAcknowledged()) {
            e.presentation.isEnabled = false
            e.presentation.text = TextBundle.message("navigation.unsupported.notSupported")
            e.presentation.description = TextBundle.message("navigation.unsupported.notSupported")
        } else {
            e.presentation.isEnabled = hasSelectedWorkflow
            e.presentation.text = TextBundle.message("action.goToDefinition")
            e.presentation.description = TextBundle.message("action.goToDefinition.description")
        }
    }

    private fun isTooltipAcknowledged(): Boolean {
        return PropertiesComponent.getInstance().getInt(PROPERTY_KEY, 0) > 0
    }

    private fun showUnsupportedLanguageTooltip(e: AnActionEvent) {
        val component = e.inputEvent?.component as? JComponent ?: return

        val languageName = IdeLanguageSupport.getUnsupportedLanguageName()

        val message = if (languageName != null) {
            TextBundle.message("navigation.unsupported.language", languageName)
        } else {
            TextBundle.message("navigation.unsupported.generic")
        }

        GotItTooltip(TOOLTIP_ID, message, project)
            .withPosition(Balloon.Position.below)
            .withBrowserLink(
                TextBundle.message("navigation.unsupported.contribute"),
                URI(IdeLanguageSupport.getContributionUrl()).toURL()
            )
            .show(component, GotItTooltip.BOTTOM_MIDDLE)
    }
}
