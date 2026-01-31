package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowDefinitionFinder
import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.goide.GoFileType
import com.goide.psi.GoFile
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
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
        return searchInFiles(project, scope) { goFile ->
            strategies.flatMap { it.findMatches(goFile, workflowType) }
        }
    }

    override fun findAllWorkflowMatches(
        project: Project,
        scope: GlobalSearchScope
    ): List<WorkflowMatch> {
        return searchInFiles(project, scope) { goFile ->
            strategies.flatMap { it.findMatches(goFile, null) }
        }
    }

    private fun searchInFiles(
        project: Project,
        scope: GlobalSearchScope,
        search: (GoFile) -> List<WorkflowMatch>
    ): List<WorkflowMatch> {
        val results = mutableListOf<WorkflowMatch>()
        val psiManager = PsiManager.getInstance(project)

        FileTypeIndex.getFiles(GoFileType.INSTANCE, scope).forEach { virtualFile ->
            val goFile = psiManager.findFile(virtualFile) as? GoFile ?: return@forEach
            results.addAll(search(goFile))
        }

        return deduplicateMatches(results)
    }

    private fun deduplicateMatches(matches: List<WorkflowMatch>): List<WorkflowMatch> {
        return matches.distinctBy { "${it.fileName}:${it.lineNumber}" }
    }
}