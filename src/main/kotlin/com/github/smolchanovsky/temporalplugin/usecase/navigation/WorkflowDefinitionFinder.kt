package com.github.smolchanovsky.temporalplugin.usecase.navigation

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

interface WorkflowDefinitionFinder {
    fun getSupportedFileExtensions(): Set<String>
    fun getLanguageName(): String

    fun findWorkflowMatches(
        project: Project,
        workflowType: String,
        scope: GlobalSearchScope
    ): List<WorkflowMatch>

    fun findAllWorkflowMatches(
        project: Project,
        scope: GlobalSearchScope
    ): List<WorkflowMatch>
}
