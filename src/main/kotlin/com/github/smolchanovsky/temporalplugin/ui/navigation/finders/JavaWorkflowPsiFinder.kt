package com.github.smolchanovsky.temporalplugin.ui.navigation.finders

import com.github.smolchanovsky.temporalplugin.ui.navigation.WorkflowDefinitionFinder
import com.github.smolchanovsky.temporalplugin.ui.navigation.WorkflowNavigationItem
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch

class JavaWorkflowPsiFinder : WorkflowDefinitionFinder {

    companion object {
        const val WORKFLOW_INTERFACE_FQN = "io.temporal.workflow.WorkflowInterface"
        const val WORKFLOW_METHOD_FQN = "io.temporal.workflow.WorkflowMethod"
    }

    override fun getSupportedFileExtensions(): Set<String> = setOf("java", "kt")

    override fun getLanguageName(): String = "Java/Kotlin"

    override fun findNavigationItems(
        project: Project,
        workflowType: String,
        scope: GlobalSearchScope
    ): List<WorkflowNavigationItem> {
        val results = mutableListOf<WorkflowNavigationItem>()
        val allScope = GlobalSearchScope.allScope(project)

        val workflowInterfaceClass = findAnnotationClass(project, WORKFLOW_INTERFACE_FQN, allScope)
        if (workflowInterfaceClass != null) {
            AnnotatedElementsSearch.searchPsiClasses(workflowInterfaceClass, scope)
                .filter { psiClass ->
                    psiClass.name == workflowType || getWorkflowMethodName(psiClass) == workflowType
                }
                .mapTo(results) { createNavigationItem(it) }
        }

        val workflowMethodClass = findAnnotationClass(project, WORKFLOW_METHOD_FQN, allScope)
        if (workflowMethodClass != null) {
            AnnotatedElementsSearch.searchPsiMethods(workflowMethodClass, scope)
                .filter { method ->
                    val annotationName = getWorkflowMethodAnnotationName(method)
                    annotationName == workflowType || (annotationName == null && method.name == workflowType)
                }
                .mapTo(results) { createNavigationItemFromMethod(it) }
        }

        return results
    }

    override fun findAllNavigationItems(
        project: Project,
        scope: GlobalSearchScope
    ): List<WorkflowNavigationItem> {
        val results = mutableListOf<WorkflowNavigationItem>()
        val allScope = GlobalSearchScope.allScope(project)

        val workflowInterfaceClass = findAnnotationClass(project, WORKFLOW_INTERFACE_FQN, allScope)
        if (workflowInterfaceClass != null) {
            AnnotatedElementsSearch.searchPsiClasses(workflowInterfaceClass, scope)
                .mapTo(results) { createNavigationItem(it) }
        }

        return results
    }

    private fun findAnnotationClass(project: Project, fqn: String, scope: GlobalSearchScope): PsiClass? {
        return JavaPsiFacade.getInstance(project).findClass(fqn, scope)
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

    private fun getWorkflowMethodAnnotationName(method: PsiMethod): String? {
        val annotation = method.getAnnotation(WORKFLOW_METHOD_FQN) ?: return null
        val nameValue = annotation.findAttributeValue("name")
        val name = nameValue?.text?.removeSurrounding("\"")
        return if (!name.isNullOrEmpty()) name else null
    }

    private fun createNavigationItem(psiClass: PsiClass): WorkflowNavigationItem {
        val workflowType = getWorkflowMethodName(psiClass) ?: psiClass.name ?: "Unknown"
        val namespace = (psiClass.containingFile as? PsiJavaFile)?.packageName

        return WorkflowNavigationItem(
            element = psiClass.nameIdentifier ?: psiClass,
            workflowType = workflowType,
            definitionType = "interface",
            language = getLanguageName(),
            namespace = namespace
        )
    }

    private fun createNavigationItemFromMethod(method: PsiMethod): WorkflowNavigationItem {
        val annotationName = getWorkflowMethodAnnotationName(method)
        val workflowType = annotationName ?: method.name
        val namespace = (method.containingFile as? PsiJavaFile)?.packageName

        return WorkflowNavigationItem(
            element = method.nameIdentifier ?: method,
            workflowType = workflowType,
            definitionType = "method",
            language = getLanguageName(),
            namespace = namespace
        )
    }
}
