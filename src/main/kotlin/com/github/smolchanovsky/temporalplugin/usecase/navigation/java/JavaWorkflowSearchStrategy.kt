package com.github.smolchanovsky.temporalplugin.usecase.navigation.java

import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

interface JavaWorkflowSearchStrategy {
    fun findMatches(project: Project, scope: GlobalSearchScope, workflowType: String?): List<WorkflowMatch>
}
