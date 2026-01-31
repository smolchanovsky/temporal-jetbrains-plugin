package com.github.smolchanovsky.temporalplugin.usecase.navigation

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker

class CachingWorkflowFinder(
    private val delegate: WorkflowDefinitionFinder
) : WorkflowDefinitionFinder {

    private val cacheKey: Key<CachedValue<List<WorkflowMatch>>> = Key.create(
        "CachingWorkflowFinder.${delegate.javaClass.name}"
    )

    override fun getSupportedFileExtensions(): Set<String> = delegate.getSupportedFileExtensions()

    override fun getLanguageName(): String = delegate.getLanguageName()

    override fun findWorkflowMatches(
        project: Project,
        workflowType: String,
        scope: GlobalSearchScope
    ): List<WorkflowMatch> {
        // Exact search is not cached - it's specific to workflowType
        return delegate.findWorkflowMatches(project, workflowType, scope)
    }

    override fun findAllWorkflowMatches(
        project: Project,
        scope: GlobalSearchScope
    ): List<WorkflowMatch> {
        return CachedValuesManager.getManager(project).getCachedValue(project, cacheKey, {
            val results = delegate.findAllWorkflowMatches(project, scope)
            CachedValueProvider.Result.create(
                results,
                PsiModificationTracker.getInstance(project)
            )
        }, false)
    }
}
