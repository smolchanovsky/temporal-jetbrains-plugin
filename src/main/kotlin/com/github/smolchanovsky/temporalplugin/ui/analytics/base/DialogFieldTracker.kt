package com.github.smolchanovsky.temporalplugin.ui.analytics.base

import com.github.smolchanovsky.temporalplugin.analytics.AnalyticsService
import com.github.smolchanovsky.temporalplugin.analytics.DialogFieldChangedEvent
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.ui.LanguageTextField
import javax.swing.event.DocumentEvent as SwingDocumentEvent
import javax.swing.event.DocumentListener as SwingDocumentListener
import javax.swing.text.JTextComponent

class DialogFieldTracker(private val dialogName: String) {

    private val trackedFields = mutableSetOf<String>()
    private val analytics = AnalyticsService.getInstance()

    fun createSwingListener(fieldName: String): SwingDocumentListener = object : SwingDocumentListener {
        override fun insertUpdate(e: SwingDocumentEvent?) = trackField(fieldName)
        override fun removeUpdate(e: SwingDocumentEvent?) = trackField(fieldName)
        override fun changedUpdate(e: SwingDocumentEvent?) = trackField(fieldName)
    }

    fun createEditorListener(fieldName: String): DocumentListener = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            trackField(fieldName)
        }
    }

    fun track(component: JTextComponent, fieldName: String) {
        component.document.addDocumentListener(createSwingListener(fieldName))
    }

    fun track(component: LanguageTextField, fieldName: String) {
        component.document.addDocumentListener(createEditorListener(fieldName))
    }

    private fun trackField(fieldName: String) {
        if (trackedFields.add(fieldName)) {
            analytics.track(DialogFieldChangedEvent(dialogName, fieldName))
        }
    }
}
