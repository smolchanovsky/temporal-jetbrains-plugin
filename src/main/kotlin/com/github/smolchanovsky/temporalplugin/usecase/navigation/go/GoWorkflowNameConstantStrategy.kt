package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.goide.psi.GoConstDefinition
import com.goide.psi.GoConstSpec
import com.goide.psi.GoFile
import com.goide.psi.GoMethodDeclaration
import com.goide.psi.GoSignatureOwner
import com.goide.psi.GoStringLiteral
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil

class GoWorkflowNameConstantStrategy : GoWorkflowSearchStrategy {

    companion object {
        private const val WORKFLOW_CONTEXT_TYPE = "workflow.Context"
        private const val LANGUAGE = "Go"
        private val WORKFLOW_NAME_PATTERNS = listOf(
            "workflowname",
            "workflow_name",
            "workflowtype",
            "workflow_type"
        )
    }

    override fun findMatches(goFile: GoFile, workflowType: String?): List<WorkflowMatch> {
        val workflowConstants = findWorkflowNameConstants(goFile)
        if (workflowConstants.isEmpty()) return emptyList()

        return workflowConstants.flatMap { (_, constantValue) ->
            if (workflowType != null && constantValue != workflowType) {
                return@flatMap emptyList()
            }

            // First try to find methods in the same file
            val methodsInFile = findWorkflowMethods(goFile)
            if (methodsInFile.isNotEmpty()) {
                return@flatMap methodsInFile.map { method ->
                    createMatch(method, constantValue, method.containingFile as GoFile)
                }
            }

            // Then try to find methods in the same package
            val methodsInPackage = findWorkflowMethodsInPackage(goFile)
            methodsInPackage.map { method ->
                createMatch(method, constantValue, method.containingFile as GoFile)
            }
        }
    }

    private fun findWorkflowMethodsInPackage(goFile: GoFile): List<GoMethodDeclaration> {
        val directory = goFile.containingDirectory ?: return emptyList()
        val psiManager = PsiManager.getInstance(goFile.project)

        val packageFiles = directory.virtualFile.children
            .filter { it.extension == "go" && it != goFile.virtualFile }
            .mapNotNull { psiManager.findFile(it) as? GoFile }

        return packageFiles.flatMap { findWorkflowMethods(it) }
    }

    private fun findWorkflowNameConstants(goFile: GoFile): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()

        PsiTreeUtil.findChildrenOfType(goFile, GoConstDefinition::class.java).forEach { constDef ->
            val name = constDef.name ?: return@forEach
            if (!isWorkflowNameConstant(name)) return@forEach

            val value = resolveConstantValue(constDef) ?: return@forEach
            results.add(name to value)
        }

        return results
    }

    private fun isWorkflowNameConstant(name: String): Boolean {
        val lowerName = name.lowercase()
        return WORKFLOW_NAME_PATTERNS.any { pattern -> lowerName == pattern }
    }

    private fun resolveConstantValue(constDef: GoConstDefinition): String? {
        val constSpec = constDef.parent as? GoConstSpec ?: return null
        val index = constSpec.constDefinitionList.indexOf(constDef)
        val expr = constSpec.expressionList.getOrNull(index) ?: return null

        return when (expr) {
            is GoStringLiteral -> expr.decodedText
            else -> null
        }
    }

    private fun findWorkflowMethods(goFile: GoFile): List<GoMethodDeclaration> {
        return goFile.methods.filter { hasWorkflowContextParameter(it) }
    }

    private fun hasWorkflowContextParameter(signatureOwner: GoSignatureOwner): Boolean {
        val signature = signatureOwner.signature ?: return false
        val parameters = signature.parameters?.parameterDeclarationList ?: return false

        if (parameters.isEmpty()) return false

        val firstParam = parameters.first()
        val paramType = firstParam.type?.text ?: return false

        return paramType == WORKFLOW_CONTEXT_TYPE
    }

    private fun createMatch(
        method: GoMethodDeclaration,
        workflowName: String,
        goFile: GoFile
    ): WorkflowMatch {
        return WorkflowMatch(
            element = method.identifier ?: method,
            workflowType = workflowName,
            definitionType = "workflow method",
            language = LANGUAGE,
            namespace = goFile.packageName
        )
    }
}