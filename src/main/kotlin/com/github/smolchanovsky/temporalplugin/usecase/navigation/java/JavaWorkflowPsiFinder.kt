package com.github.smolchanovsky.temporalplugin.usecase.navigation.java

import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowDefinitionFinder
import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

class JavaWorkflowPsiFinder : WorkflowDefinitionFinder {

    private val strategies: List<JavaWorkflowSearchStrategy> = listOf(
        JavaAnnotationWorkflowStrategy(),
        JavaSpringWorkflowImplStrategy()
    )

    override fun getSupportedFileExtensions(): Set<String> = setOf("java", "kt")

    override fun getLanguageName(): String = "Java/Kotlin"

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
