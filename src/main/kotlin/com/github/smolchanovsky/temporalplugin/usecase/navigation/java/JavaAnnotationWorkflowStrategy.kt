package com.github.smolchanovsky.temporalplugin.usecase.navigation.java

import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.search.searches.ClassInheritorsSearch

class JavaAnnotationWorkflowStrategy : JavaWorkflowSearchStrategy {

    companion object {
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
        val workflowInterfaceAnnotation = JavaPsiFacade.getInstance(project)
            .findClass(WORKFLOW_INTERFACE_FQN, allScope) ?: return emptyList()

        val results = mutableListOf<WorkflowMatch>()

        // Find all @WorkflowInterface annotated interfaces
        AnnotatedElementsSearch.searchPsiClasses(workflowInterfaceAnnotation, allScope)
            .filter { interfaceClass ->
                workflowType == null || matchesWorkflowType(interfaceClass, workflowType)
            }
            .forEach { interfaceClass ->
                // Find implementations of this interface within the search scope
                ClassInheritorsSearch.search(interfaceClass, scope, true)
                    .filter { !it.isInterface }
                    .mapTo(results) { implClass -> createMatch(implClass, interfaceClass) }
            }

        return results
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
