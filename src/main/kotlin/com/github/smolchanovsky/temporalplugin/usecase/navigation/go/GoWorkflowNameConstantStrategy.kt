package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.goide.GoFileType
import com.goide.psi.GoConstDefinition
import com.goide.psi.GoConstSpec
import com.goide.psi.GoFile
import com.goide.psi.GoMethodDeclaration
import com.goide.psi.GoSignatureOwner
import com.goide.psi.GoStringLiteral
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
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

    override fun findMatches(project: Project, scope: GlobalSearchScope, workflowType: String?): List<WorkflowMatch> {
        val psiManager = PsiManager.getInstance(project)

        // Group files by package
        val filesByPackage = mutableMapOf<String, MutableList<GoFile>>()

        FileTypeIndex.getFiles(GoFileType.INSTANCE, scope).forEach { virtualFile ->
            val goFile = psiManager.findFile(virtualFile) as? GoFile ?: return@forEach
            val packageName = goFile.packageName ?: return@forEach
            filesByPackage.getOrPut(packageName) { mutableListOf() }.add(goFile)
        }

        val results = mutableListOf<WorkflowMatch>()

        // For each package, find constants and methods
        filesByPackage.forEach { (_, packageFiles) ->
            val packageConstants = packageFiles.flatMap { findWorkflowNameConstants(it) }
            val packageMethods = packageFiles.flatMap { findWorkflowMethods(it) }

            if (packageConstants.isEmpty() || packageMethods.isEmpty()) return@forEach

            packageConstants.forEach { (_, constantValue) ->
                if (workflowType != null && constantValue != workflowType) {
                    return@forEach
                }

                // Use the first method in the package as the navigation target
                val method = packageMethods.first()
                results.add(createMatch(method, constantValue, method.containingFile as GoFile))
            }
        }

        return results
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