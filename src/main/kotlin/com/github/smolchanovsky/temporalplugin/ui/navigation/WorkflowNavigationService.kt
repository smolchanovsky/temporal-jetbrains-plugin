package com.github.smolchanovsky.temporalplugin.ui.navigation

import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.usecase.navigation.FindWorkflowDefinitionRequest
import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowDefinitionFinder
import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
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

    fun navigateToWorkflowDefinition(workflowType: String) {
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
                    handleSearchResults(workflowType, matches)
                }
            }
        })
    }

    private fun handleSearchResults(workflowType: String, matches: List<WorkflowMatch>) {
        when {
            matches.isEmpty() -> {
                showNotification(TextBundle.message("navigation.not.found", workflowType))
            }
            matches.size == 1 -> {
                matches.first().navigate(true)
            }
            else -> {
                WorkflowNavigationPopup.show(
                    project,
                    TextBundle.message("navigation.popup.multiple", workflowType),
                    matches
                )
            }
        }
    }

    private fun showNotification(message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("TemporalWorkflowNavigation")
            .createNotification(message, NotificationType.INFORMATION)
            .notify(project)
    }
}
