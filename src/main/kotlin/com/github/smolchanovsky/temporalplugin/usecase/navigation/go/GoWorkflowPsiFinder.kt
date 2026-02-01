package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowDefinitionFinder
import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

class GoWorkflowPsiFinder : WorkflowDefinitionFinder {

    private val strategies: List<GoWorkflowSearchStrategy> = listOf(
        GoWorkflowDefinitionStrategy(),
        GoWorkflowRegistrationStrategy(),
        GoWorkflowNameConstantStrategy()
    )

    override fun getSupportedFileExtensions(): Set<String> = setOf("go")

    override fun getLanguageName(): String = "Go"

    override fun findWorkflowMatches(
        project: Project,
        workflowType: String,
        scope: GlobalSearchScope
    ): List<WorkflowMatch> {
        val results = strategies.flatMap { it.findMatches(project, scope, workflowType) }
        return deduplicateMatches(results)
    }

    override fun findAllWorkflowMatches(
        project: Project,
        scope: GlobalSearchScope
    ): List<WorkflowMatch> {
        val results = strategies.flatMap { it.findMatches(project, scope, null) }
        return deduplicateMatches(results)
    }

    private fun deduplicateMatches(matches: List<WorkflowMatch>): List<WorkflowMatch> {
        return matches.distinctBy { "${it.fileName}:${it.lineNumber}" }
    }
}