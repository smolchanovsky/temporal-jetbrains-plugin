package com.github.smolchanovsky.temporalplugin.ui.common

import com.intellij.json.JsonFileType
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.awt.BorderLayout
import javax.swing.JPanel

class JsonViewerPanel(private val project: Project) : JPanel(BorderLayout()), Disposable {

    private val json = Json { prettyPrint = true }
    private var editor: EditorEx? = null

    fun setJson(jsonString: String?) {
        val content = jsonString?.let { formatJson(it) } ?: ""

        if (editor == null) {
            createEditor(content)
        } else {
            updateEditor(content)
        }
    }

    private fun formatJson(jsonString: String): String {
        return try {
            val element = json.decodeFromString<JsonElement>(jsonString.trim())
            json.encodeToString(JsonElement.serializer(), element)
        } catch (e: Exception) {
            jsonString
        }
    }

    private fun createEditor(content: String) {
        val virtualFile = LightVirtualFile("data.json", JsonFileType.INSTANCE, content)
        val document = EditorFactory.getInstance().createDocument(content)

        editor = (EditorFactory.getInstance().createViewer(document, project) as EditorEx).apply {
            highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(
                project, virtualFile
            )
            settings.isLineNumbersShown = false
            settings.isFoldingOutlineShown = true
            settings.isUseSoftWraps = true
        }
        add(editor!!.component, BorderLayout.CENTER)
    }

    private fun updateEditor(content: String) {
        editor?.let { EditorFactory.getInstance().releaseEditor(it) }
        removeAll()
        createEditor(content)
        revalidate()
        repaint()
    }

    override fun dispose() {
        editor?.let { EditorFactory.getInstance().releaseEditor(it) }
        editor = null
    }
}
