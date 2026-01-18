package com.github.smolchanovsky.temporalplugin.ui.details

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.domain.WorkflowEvent
import com.intellij.icons.AllIcons
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.JTextArea

class EventDetailsPanel(event: WorkflowEvent) : JPanel(BorderLayout()) {

    init {
        val builder = FormBuilder.createFormBuilder()

        event.activityName?.let { builder.addRow("Activity:", it) }
        event.timerId?.let { builder.addRow("Timer ID:", it) }
        event.failureMessage?.let { builder.addRow("Failure:", it) }

        for (attr in event.attributes) {
            builder.addRow(attr.label + ":", attr.value)
        }

        if (builder.panel.componentCount == 0) {
            builder.addComponent(JBLabel(TextBundle.message("details.noDetails")).apply {
                foreground = JBUI.CurrentTheme.Label.disabledForeground()
            })
        }

        val content = builder.panel.apply {
            border = JBUI.Borders.empty(8, 16)
            background = JBColor(0xFAFAFA, 0x2B2D30)
        }

        add(content, BorderLayout.CENTER)
        background = JBColor(0xFAFAFA, 0x2B2D30)
    }

    private fun FormBuilder.addRow(label: String, value: String): FormBuilder {
        val normalizedValue = value.replace("\n", " ")

        val valueLabel = JBLabel(normalizedValue).apply {
            toolTipText = TextBundle.message("details.clickToCopy")
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    copyToClipboard(value, e)
                }
            })
        }

        val labelWrapper = object : JPanel(BorderLayout()) {
            override fun getPreferredSize(): Dimension {
                val pref = super.getPreferredSize()
                return Dimension(0, pref.height)
            }
        }.apply {
            isOpaque = false
            add(valueLabel, BorderLayout.CENTER)
        }

        val expandIcon = JBLabel(AllIcons.General.ExpandComponent).apply {
            toolTipText = TextBundle.message("details.showFullValue")
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            border = JBUI.Borders.emptyLeft(4)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    showValuePopup(value, e)
                }
            })
        }

        val wrapper = JPanel(BorderLayout()).apply {
            isOpaque = false
            add(labelWrapper, BorderLayout.CENTER)
            add(expandIcon, BorderLayout.EAST)
        }

        return addLabeledComponent(
            JBLabel(label).apply {
                foreground = JBUI.CurrentTheme.Label.disabledForeground()
            },
            wrapper
        )
    }

    private fun copyToClipboard(value: String, e: MouseEvent) {
        CopyPasteManager.getInstance().setContents(StringSelection(value))

        JBPopupFactory.getInstance()
            .createBalloonBuilder(JBLabel(TextBundle.message("details.copied")))
            .setFadeoutTime(1500)
            .setBorderColor(JBColor.background())
            .setFillColor(JBColor.background())
            .createBalloon()
            .show(RelativePoint.getSouthWestOf(e.component as javax.swing.JComponent), Balloon.Position.below)
    }

    private fun showValuePopup(value: String, e: MouseEvent) {
        val textArea = JTextArea(value).apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            border = JBUI.Borders.empty(8)
        }

        val copyButton = JBLabel(AllIcons.Actions.Copy).apply {
            toolTipText = TextBundle.message("details.copied")
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    CopyPasteManager.getInstance().setContents(StringSelection(value))
                }
            })
        }

        val bottomPanel = JPanel(BorderLayout()).apply {
            isOpaque = false
            border = JBUI.Borders.empty(8, 12)
            add(copyButton, BorderLayout.EAST)
        }

        val panel = JPanel(BorderLayout()).apply {
            add(JBScrollPane(textArea).apply {
                preferredSize = Dimension(500, 300)
                border = null
            }, BorderLayout.CENTER)
            add(bottomPanel, BorderLayout.SOUTH)
        }

        JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, textArea)
            .setRequestFocus(true)
            .setResizable(true)
            .setMovable(true)
            .createPopup()
            .showUnderneathOf(e.component)
    }
}
