package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.goide.psi.GoFile
import com.goide.psi.GoFunctionDeclaration
import com.goide.psi.GoMethodDeclaration
import com.goide.psi.GoSignatureOwner

class GoWorkflowDefinitionStrategy : GoWorkflowSearchStrategy {

    companion object {
        private const val WORKFLOW_CONTEXT_TYPE = "workflow.Context"
        private const val LANGUAGE = "Go"
    }

    override fun findMatches(goFile: GoFile, workflowType: String?): List<WorkflowMatch> {
        val results = mutableListOf<WorkflowMatch>()

        goFile.functions
            .filter { hasWorkflowContextParameter(it) }
            .filter { workflowType == null || it.name == workflowType }
            .mapTo(results) { createMatch(it, goFile) }

        goFile.methods
            .filter { hasWorkflowContextParameter(it) }
            .filter { workflowType == null || it.name == workflowType }
            .mapTo(results) { createMatch(it, goFile) }

        return results
    }

    private fun hasWorkflowContextParameter(signatureOwner: GoSignatureOwner): Boolean {
        val signature = signatureOwner.signature ?: return false
        val parameters = signature.parameters.parameterDeclarationList ?: return false

        if (parameters.isEmpty()) return false

        val firstParam = parameters.first()
        val paramType = firstParam.type?.text ?: return false

        return paramType == WORKFLOW_CONTEXT_TYPE
    }

    private fun createMatch(function: GoFunctionDeclaration, goFile: GoFile): WorkflowMatch {
        return WorkflowMatch(
            element = function.identifier,
            workflowType = function.name ?: "Unknown",
            definitionType = "function",
            language = LANGUAGE,
            namespace = goFile.packageName
        )
    }

    private fun createMatch(method: GoMethodDeclaration, goFile: GoFile): WorkflowMatch {
        return WorkflowMatch(
            element = method.identifier ?: method,
            workflowType = method.name ?: "Unknown",
            definitionType = "method",
            language = LANGUAGE,
            namespace = goFile.packageName
        )
    }
}
