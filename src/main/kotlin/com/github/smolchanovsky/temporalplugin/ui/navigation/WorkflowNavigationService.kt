package com.github.smolchanovsky.temporalplugin.ui.navigation

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker

@Service(Service.Level.PROJECT)
class WorkflowNavigationService(private val project: Project) {

    companion object {
        val EP_NAME: ExtensionPointName<WorkflowDefinitionFinder> = ExtensionPointName.create(
            "com.github.smolchanovsky.temporalplugin.workflowDefinitionFinder"
        )
    }

    private val finders: List<WorkflowDefinitionFinder>
        get() = EP_NAME.extensionList

    fun hasFinders(): Boolean = finders.isNotEmpty()

    fun navigateToWorkflowDefinition(workflowType: String) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            TextBundle.message("navigation.searching", workflowType),
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true

                val scope = GlobalSearchScope.projectScope(project)

                indicator.text = TextBundle.message("navigation.searching.exact")

                val matches = ReadAction.compute<List<WorkflowNavigationItem>, Throwable> {
                    finders.flatMap { it.findNavigationItems(project, workflowType, scope) }
                }

                ApplicationManager.getApplication().invokeLater {
                    handleSearchResults(workflowType, matches)
                }
            }
        })
    }

    private fun handleSearchResults(
        workflowType: String,
        matches: List<WorkflowNavigationItem>
    ) {
        when {
            matches.size == 1 -> {
                matches.first().navigate(true)
            }
            matches.size > 1 -> {
                WorkflowNavigationPopup.show(
                    project,
                    TextBundle.message("navigation.popup.multiple", workflowType),
                    matches
                )
            }
            else -> {
                performFallbackSearch(workflowType)
            }
        }
    }

    private fun performFallbackSearch(workflowType: String) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            TextBundle.message("navigation.searching.all"),
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true

                val scope = GlobalSearchScope.projectScope(project)

                val allWorkflows = ReadAction.compute<List<WorkflowNavigationItem>, Throwable> {
                    getCachedWorkflows(scope)
                }

                ApplicationManager.getApplication().invokeLater {
                    if (allWorkflows.isEmpty()) {
                        showNotification(TextBundle.message("navigation.not.found", workflowType))
                    } else {
                        WorkflowNavigationPopup.show(
                            project,
                            TextBundle.message("navigation.popup.fallback", workflowType),
                            allWorkflows
                        )
                    }
                }
            }
        })
    }

    private fun getCachedWorkflows(scope: GlobalSearchScope): List<WorkflowNavigationItem> {
        return CachedValuesManager.getManager(project).getCachedValue(project) {
            val results = finders.flatMap { it.findAllNavigationItems(project, scope) }
            CachedValueProvider.Result.create(
                results,
                PsiModificationTracker.getInstance(project)
            )
        }
    }

    private fun showNotification(message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("TemporalWorkflowNavigation")
            .createNotification(message, NotificationType.INFORMATION)
            .notify(project)
    }
}
