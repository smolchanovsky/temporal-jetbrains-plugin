package com.github.smolchanovsky.temporalplugin.ui.navigation

import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import javax.swing.Icon

class WorkflowNavigationItem(
    element: PsiElement,
    val workflowType: String,
    val definitionType: String,
    val language: String,
    val namespace: String?
) : NavigationItem, Navigatable {

    private val pointer: SmartPsiElementPointer<PsiElement> =
        SmartPointerManager.createPointer(element)

    private val fileName: String = element.containingFile?.virtualFile?.name ?: ""

    private val lineNumber: Int = element.containingFile?.viewProvider?.document
        ?.getLineNumber(element.textOffset)?.plus(1) ?: 0

    val displayPresentation: String = buildString {
        append(workflowType)
        if (!namespace.isNullOrEmpty()) {
            append(" ($namespace)")
        }
        append(" - ")
        append(fileName)
        if (lineNumber > 0) {
            append(":$lineNumber")
        }
    }

    val element: PsiElement?
        get() = pointer.element

    override fun getName(): String = workflowType

    override fun getPresentation(): ItemPresentation = object : ItemPresentation {
        override fun getPresentableText(): String = workflowType

        override fun getLocationString(): String {
            val ns = namespace?.let { "($it)" } ?: ""
            return "$ns - $fileName:$lineNumber"
        }

        override fun getIcon(unused: Boolean): Icon? = null
    }

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
