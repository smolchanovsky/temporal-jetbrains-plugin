package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.goide.GoFileType
import com.goide.psi.GoCallExpr
import com.goide.psi.GoCompositeLit
import com.goide.psi.GoFile
import com.goide.psi.GoFunctionDeclaration
import com.goide.psi.GoReferenceExpression
import com.goide.psi.GoConstDefinition
import com.goide.psi.GoConstSpec
import com.goide.psi.GoStringLiteral
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil

class GoWorkflowRegistrationStrategy : GoWorkflowSearchStrategy {

    companion object {
        private const val REGISTER_METHOD = "RegisterWorkflowWithOptions"
        private const val NAME_FIELD = "Name"
        private const val LANGUAGE = "Go"
    }

    override fun findMatches(project: Project, scope: GlobalSearchScope, workflowType: String?): List<WorkflowMatch> {
        val results = mutableListOf<WorkflowMatch>()
        val psiManager = PsiManager.getInstance(project)

        FileTypeIndex.getFiles(GoFileType.INSTANCE, scope).forEach { virtualFile ->
            val goFile = psiManager.findFile(virtualFile) as? GoFile ?: return@forEach

            findRegistrationCalls(goFile)
                .mapNotNull { call ->
                    val registeredName = getRegisteredName(call) ?: return@mapNotNull null
                    if (workflowType != null && registeredName != workflowType) return@mapNotNull null

                    val workflowFunction = resolveWorkflowFunction(call) ?: return@mapNotNull null
                    createMatch(workflowFunction, registeredName, workflowFunction.containingFile as GoFile)
                }
                .forEach { results.add(it) }
        }

        return results
    }

    private fun findRegistrationCalls(goFile: GoFile): List<GoCallExpr> {
        return PsiTreeUtil.findChildrenOfType(goFile, GoCallExpr::class.java)
            .filter { isRegistrationCall(it) }
    }

    private fun isRegistrationCall(callExpr: GoCallExpr): Boolean {
        val expression = callExpr.expression as? GoReferenceExpression ?: return false
        val methodName = expression.identifier?.text ?: return false
        return methodName == REGISTER_METHOD
    }

    private fun getRegisteredName(callExpr: GoCallExpr): String? {
        val args = callExpr.argumentList.expressionList
        // RegisterWorkflowWithOptions(workflowFunc, RegisterOptions{Name: "..."})
        if (args.size < 2) return null

        val optionsArg = args[1] as? GoCompositeLit ?: return null
        return extractNameFromOptions(optionsArg)
    }

    private fun extractNameFromOptions(compositeLit: GoCompositeLit): String? {
        val literalValue = compositeLit.literalValue ?: return null

        for (element in literalValue.elementList) {
            val key = element.key?.fieldName?.text ?: continue
            if (key == NAME_FIELD) {
                val valueExpr = element.value?.expression ?: continue
                return resolveStringValue(valueExpr)
            }
        }
        return null
    }

    private fun resolveStringValue(expression: PsiElement): String? {
        // Direct string literal: Name: "WorkflowName"
        if (expression is GoStringLiteral) {
            return expression.decodedText
        }

        // Reference to constant: Name: WorkflowNameConst
        if (expression is GoReferenceExpression) {
            val resolved = expression.resolve()
            if (resolved is GoConstDefinition) {
                val constSpec = resolved.parent as? GoConstSpec ?: return null
                val index = constSpec.constDefinitionList.indexOf(resolved)
                val constExpr = constSpec.expressionList.getOrNull(index) ?: return null
                return resolveStringValue(constExpr)
            }
        }

        return null
    }

    private fun resolveWorkflowFunction(callExpr: GoCallExpr): GoFunctionDeclaration? {
        val args = callExpr.argumentList.expressionList
        if (args.isEmpty()) return null

        val funcRef = args[0] as? GoReferenceExpression ?: return null
        val resolved = funcRef.resolve() ?: return null

        return resolved as? GoFunctionDeclaration
    }

    private fun createMatch(
        function: GoFunctionDeclaration,
        registeredName: String,
        goFile: GoFile
    ): WorkflowMatch {
        return WorkflowMatch(
            element = function.identifier ?: function,
            workflowType = registeredName,
            definitionType = "registered workflow",
            language = LANGUAGE,
            namespace = goFile.packageName
        )
    }
}
