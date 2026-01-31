package com.github.smolchanovsky.temporalplugin.usecase.navigation

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

class CompositeWorkflowFinder(
    private val finders: List<WorkflowDefinitionFinder>
) : WorkflowDefinitionFinder {

    override fun getSupportedFileExtensions(): Set<String> =
        finders.flatMap { it.getSupportedFileExtensions() }.toSet()

    override fun getLanguageName(): String =
        finders.joinToString(", ") { it.getLanguageName() }

    override fun findWorkflowMatches(
        project: Project,
        workflowType: String,
        scope: GlobalSearchScope
    ): List<WorkflowMatch> {
        val exactMatches = finders.flatMap { it.findWorkflowMatches(project, workflowType, scope) }

        // Fallback: if no exact matches, return all workflows
        return exactMatches.ifEmpty {
            findAllWorkflowMatches(project, scope)
        }
    }

    override fun findAllWorkflowMatches(
        project: Project,
        scope: GlobalSearchScope
    ): List<WorkflowMatch> {
        return finders.flatMap { it.findAllWorkflowMatches(project, scope) }
    }
}
