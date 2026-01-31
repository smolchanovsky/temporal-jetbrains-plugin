package com.github.smolchanovsky.temporalplugin.ui.navigation

import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.usecase.navigation.FindWorkflowDefinitionRequest
import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowDefinitionFinder
import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import kotlinx.coroutines.runBlocking

@Service(Service.Level.PROJECT)
class WorkflowNavigationService(private val project: Project) {

    companion object {
        private val EP_NAME: ExtensionPointName<WorkflowDefinitionFinder> = ExtensionPointName.create(
            "com.github.smolchanovsky.temporalplugin.workflowDefinitionFinder"
        )
    }

    private val mediator by lazy { project.service<TemporalMediator>().mediator }

    fun hasFinders(): Boolean = EP_NAME.extensionList.isNotEmpty()

    fun findWorkflowDefinition(workflowType: String, onResult: (NavigationResult) -> Unit) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            TextBundle.message("navigation.searching", workflowType),
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = TextBundle.message("navigation.searching.exact")

                val matches = runBlocking {
                    mediator.send(FindWorkflowDefinitionRequest(project, workflowType))
                }

                ApplicationManager.getApplication().invokeLater {
                    onResult(toNavigationResult(workflowType, matches))
                }
            }
        })
    }

    private fun toNavigationResult(workflowType: String, matches: List<WorkflowMatch>): NavigationResult {
        return when {
            matches.isEmpty() -> NavigationResult.NotFound(workflowType)
            matches.size == 1 -> NavigationResult.SingleMatch(matches.first())
            else -> NavigationResult.MultipleMatches(
                title = TextBundle.message("navigation.popup.multiple", workflowType),
                matches = matches
            )
        }
    }
}
