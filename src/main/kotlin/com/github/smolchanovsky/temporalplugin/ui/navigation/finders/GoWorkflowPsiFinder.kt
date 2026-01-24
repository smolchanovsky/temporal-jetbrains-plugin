package com.github.smolchanovsky.temporalplugin.ui.navigation.finders

import com.github.smolchanovsky.temporalplugin.ui.navigation.WorkflowDefinitionFinder
import com.github.smolchanovsky.temporalplugin.ui.navigation.WorkflowNavigationItem
import com.goide.psi.GoFile
import com.goide.psi.GoFunctionDeclaration
import com.goide.psi.GoMethodDeclaration
import com.goide.psi.GoSignatureOwner
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.goide.GoFileType
import com.intellij.psi.PsiManager

class GoWorkflowPsiFinder : WorkflowDefinitionFinder {

    companion object {
        const val WORKFLOW_CONTEXT_TYPE = "workflow.Context"
    }

    override fun getSupportedFileExtensions(): Set<String> = setOf("go")

    override fun getLanguageName(): String = "Go"

    override fun findNavigationItems(
        project: Project,
        workflowType: String,
        scope: GlobalSearchScope
    ): List<WorkflowNavigationItem> {
        val results = mutableListOf<WorkflowNavigationItem>()
        val psiManager = PsiManager.getInstance(project)

        FileTypeIndex.getFiles(GoFileType.INSTANCE, scope).forEach { virtualFile ->
            val goFile = psiManager.findFile(virtualFile) as? GoFile ?: return@forEach

            goFile.functions.filter { it.name == workflowType && hasWorkflowContextParameter(it) }
                .mapTo(results) { createNavigationItem(it, goFile) }

            goFile.methods.filter { it.name == workflowType && hasWorkflowContextParameter(it) }
                .mapTo(results) { createNavigationItem(it, goFile) }
        }

        return results
    }

    override fun findAllNavigationItems(
        project: Project,
        scope: GlobalSearchScope
    ): List<WorkflowNavigationItem> {
        val results = mutableListOf<WorkflowNavigationItem>()
        val psiManager = PsiManager.getInstance(project)

        FileTypeIndex.getFiles(GoFileType.INSTANCE, scope).forEach { virtualFile ->
            val goFile = psiManager.findFile(virtualFile) as? GoFile ?: return@forEach

            goFile.functions.filter { hasWorkflowContextParameter(it) }
                .mapTo(results) { createNavigationItem(it, goFile) }

            goFile.methods.filter { hasWorkflowContextParameter(it) }
                .mapTo(results) { createNavigationItem(it, goFile) }
        }

        return results
    }

    private fun hasWorkflowContextParameter(signatureOwner: GoSignatureOwner): Boolean {
        val signature = signatureOwner.signature ?: return false
        val parameters = signature.parameters?.parameterDeclarationList ?: return false

        if (parameters.isEmpty()) return false

        val firstParam = parameters.first()
        val paramType = firstParam.type?.text ?: return false

        return paramType == WORKFLOW_CONTEXT_TYPE
    }

    private fun createNavigationItem(function: GoFunctionDeclaration, goFile: GoFile): WorkflowNavigationItem {
        return WorkflowNavigationItem(
            element = function.identifier ?: function,
            workflowType = function.name ?: "Unknown",
            definitionType = "function",
            language = getLanguageName(),
            namespace = goFile.packageName
        )
    }

    private fun createNavigationItem(method: GoMethodDeclaration, goFile: GoFile): WorkflowNavigationItem {
        return WorkflowNavigationItem(
            element = method.identifier ?: method,
            workflowType = method.name ?: "Unknown",
            definitionType = "method",
            language = getLanguageName(),
            namespace = goFile.packageName
        )
    }
}
