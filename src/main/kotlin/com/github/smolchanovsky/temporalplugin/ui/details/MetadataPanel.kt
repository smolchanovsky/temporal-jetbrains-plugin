package com.github.smolchanovsky.temporalplugin.ui.details

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.github.smolchanovsky.temporalplugin.ui.common.FormatUtils
import com.github.smolchanovsky.temporalplugin.ui.common.WorkflowStatusPresentation
import java.awt.FlowLayout
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class MetadataPanel(project: Project) : JPanel(BorderLayout()), Disposable {

    private val state = project.service<TemporalState>()

    private val statusIcon = JBLabel()
    private val statusField = createCopyableField()
    private val statusPanel = JPanel(FlowLayout(FlowLayout.LEFT, 2, 0)).apply {
        isOpaque = false
        add(statusIcon)
        add(statusField)
    }
    private val typeField = createCopyableField()
    private val taskQueueField = createCopyableField()
    private val runIdField = createCopyableField()
    private val startTimeField = createCopyableField()
    private val closeTimeField = createCopyableField()
    private val durationField = createCopyableField()

    private fun createCopyableField(): JBTextField {
        return JBTextField().apply {
            isEditable = false
            border = null
            background = null
            isOpaque = false
            margin = JBUI.emptyInsets()
        }
    }

    private val onViewStateChanged: (ViewState) -> Unit = { viewState ->
        if (viewState is ViewState.WorkflowDetailsView) {
            SwingUtilities.invokeLater { update(viewState) }
        }
    }

    init {
        border = JBUI.Borders.empty(10)

        val contentPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = JBUI.insets(0, 4)
        }

        var row = 0
        addRow(contentPanel, gbc, row++, TextBundle.message("details.status"), statusPanel)
        addRow(contentPanel, gbc, row++, TextBundle.message("details.type"), typeField)
        addRow(contentPanel, gbc, row++, TextBundle.message("details.taskQueue"), taskQueueField)
        addRow(contentPanel, gbc, row++, TextBundle.message("details.runId"), runIdField)
        addRow(contentPanel, gbc, row++, TextBundle.message("details.startTime"), startTimeField)
        addRow(contentPanel, gbc, row++, TextBundle.message("details.closeTime"), closeTimeField)
        addRow(contentPanel, gbc, row++, TextBundle.message("details.duration"), durationField)

        gbc.gridy = row
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        contentPanel.add(JPanel(), gbc)

        add(JBScrollPane(contentPanel).apply { border = null }, BorderLayout.CENTER)

        state.addViewStateListener(onViewStateChanged)
    }

    private fun addRow(panel: JPanel, gbc: GridBagConstraints, row: Int, label: String, valueComponent: JComponent) {
        gbc.gridy = row
        gbc.gridx = 0
        gbc.weightx = 0.0
        panel.add(JBLabel("$label:").apply {
            foreground = JBUI.CurrentTheme.Label.disabledForeground()
        }, gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(valueComponent, gbc)
    }

    private fun update(viewState: ViewState.WorkflowDetailsView) {
        val details = viewState.details
        val workflow = viewState.workflow

        val status = details?.status ?: workflow.status
        statusIcon.icon = WorkflowStatusPresentation.getIcon(status)
        statusField.text = status.displayName
        typeField.text = details?.type ?: workflow.type
        taskQueueField.text = details?.taskQueue ?: "-"
        runIdField.text = details?.runId ?: workflow.runId
        startTimeField.text = FormatUtils.formatDateTime(details?.startTime ?: workflow.startTime)
        closeTimeField.text = (details?.closeTime ?: workflow.endTime)?.let { FormatUtils.formatDateTime(it) } ?: "-"
        durationField.text = details?.executionDuration?.let { FormatUtils.formatDuration(it) } ?: "-"
    }

    override fun dispose() {
        state.removeViewStateListener(onViewStateChanged)
    }
}
