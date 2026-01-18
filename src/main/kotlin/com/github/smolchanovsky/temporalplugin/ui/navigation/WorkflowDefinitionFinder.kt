package com.github.smolchanovsky.temporalplugin.ui.navigation

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

interface WorkflowDefinitionFinder {
    fun getSupportedFileExtensions(): Set<String>
    fun getLanguageName(): String

    fun findNavigationItems(
        project: Project,
        workflowType: String,
        scope: GlobalSearchScope
    ): List<WorkflowNavigationItem>

    fun findAllNavigationItems(
        project: Project,
        scope: GlobalSearchScope
    ): List<WorkflowNavigationItem>
}
