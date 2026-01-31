package com.github.smolchanovsky.temporalplugin.usecase.navigation.java

import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch

class JavaSpringWorkflowImplStrategy : JavaWorkflowSearchStrategy {

    companion object {
        private const val WORKFLOW_IMPL_FQN = "io.temporal.spring.boot.WorkflowImpl"
        private const val WORKFLOW_INTERFACE_FQN = "io.temporal.workflow.WorkflowInterface"
        private const val WORKFLOW_METHOD_FQN = "io.temporal.workflow.WorkflowMethod"
        private const val LANGUAGE = "Java/Kotlin"
    }

    override fun findMatches(
        project: Project,
        scope: GlobalSearchScope,
        workflowType: String?
    ): List<WorkflowMatch> {
        val allScope = GlobalSearchScope.allScope(project)
        val workflowImplClass = JavaPsiFacade.getInstance(project)
            .findClass(WORKFLOW_IMPL_FQN, allScope) ?: return emptyList()

        return AnnotatedElementsSearch.searchPsiClasses(workflowImplClass, scope)
            .mapNotNull { implClass -> findWorkflowInterface(implClass) }
            .filter { (_, interfaceClass) ->
                workflowType == null || matchesWorkflowType(interfaceClass, workflowType)
            }
            .map { (implClass, interfaceClass) -> createMatch(implClass, interfaceClass) }
    }

    private fun findWorkflowInterface(implClass: PsiClass): Pair<PsiClass, PsiClass>? {
        for (interfaceClass in implClass.interfaces) {
            if (interfaceClass.hasAnnotation(WORKFLOW_INTERFACE_FQN)) {
                return implClass to interfaceClass
            }
        }
        return null
    }

    private fun matchesWorkflowType(interfaceClass: PsiClass, workflowType: String): Boolean {
        return interfaceClass.name == workflowType || getWorkflowMethodName(interfaceClass) == workflowType
    }

    private fun getWorkflowMethodName(psiClass: PsiClass): String? {
        for (method in psiClass.methods) {
            val annotation = method.getAnnotation(WORKFLOW_METHOD_FQN)
            if (annotation != null) {
                val nameValue = annotation.findAttributeValue("name")
                val name = nameValue?.text?.removeSurrounding("\"")
                if (!name.isNullOrEmpty()) {
                    return name
                }
            }
        }
        return null
    }

    private fun createMatch(implClass: PsiClass, interfaceClass: PsiClass): WorkflowMatch {
        val workflowType = getWorkflowMethodName(interfaceClass) ?: interfaceClass.name ?: "Unknown"
        val namespace = (implClass.containingFile as? PsiJavaFile)?.packageName

        return WorkflowMatch(
            element = implClass.nameIdentifier ?: implClass,
            workflowType = workflowType,
            definitionType = "implementation",
            language = LANGUAGE,
            namespace = namespace
        )
    }
}
