package com.github.smolchanovsky.temporalplugin.usecase.navigation

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer

class WorkflowMatch(
    element: PsiElement,
    val workflowType: String,
    val definitionType: String,
    val language: String,
    val namespace: String?
) : Navigatable {

    private val pointer: SmartPsiElementPointer<PsiElement> =
        SmartPointerManager.createPointer(element)

    val fileName: String = element.containingFile?.virtualFile?.name ?: ""

    val lineNumber: Int = element.containingFile?.viewProvider?.document
        ?.getLineNumber(element.textOffset)?.plus(1) ?: 0

    val element: PsiElement?
        get() = pointer.element

    override fun navigate(requestFocus: Boolean) {
        val el = element ?: return
        val project = el.project
        val file = el.containingFile?.virtualFile ?: return
        val descriptor = OpenFileDescriptor(project, file, el.textOffset)
        FileEditorManager.getInstance(project).openTextEditor(descriptor, requestFocus)
    }

    override fun canNavigate(): Boolean = pointer.virtualFile != null

    override fun canNavigateToSource(): Boolean = canNavigate()
}
