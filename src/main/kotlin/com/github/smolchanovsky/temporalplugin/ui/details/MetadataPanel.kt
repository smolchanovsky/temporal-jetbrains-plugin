package com.github.smolchanovsky.temporalplugin.ui.details

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.github.smolchanovsky.temporalplugin.ui.common.FormatUtils
import com.github.smolchanovsky.temporalplugin.ui.common.WorkflowStatusPresentation
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JPanel
import javax.swing.SwingUtilities

class MetadataPanel(project: Project) : JPanel(BorderLayout()), Disposable {

    private val state = project.service<TemporalState>()

    private val statusLabel = JBLabel()
    private val typeLabel = JBLabel()
    private val taskQueueLabel = JBLabel()
    private val runIdLabel = JBLabel()
    private val startTimeLabel = JBLabel()
    private val closeTimeLabel = JBLabel()
    private val durationLabel = JBLabel()

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
            insets = JBUI.insets(4)
        }

        var row = 0
        addRow(contentPanel, gbc, row++, TextBundle.message("details.status"), statusLabel)
        addRow(contentPanel, gbc, row++, TextBundle.message("details.type"), typeLabel)
        addRow(contentPanel, gbc, row++, TextBundle.message("details.taskQueue"), taskQueueLabel)
        addRow(contentPanel, gbc, row++, TextBundle.message("details.runId"), runIdLabel)
        addRow(contentPanel, gbc, row++, TextBundle.message("details.startTime"), startTimeLabel)
        addRow(contentPanel, gbc, row++, TextBundle.message("details.closeTime"), closeTimeLabel)
        addRow(contentPanel, gbc, row++, TextBundle.message("details.duration"), durationLabel)

        gbc.gridy = row
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        contentPanel.add(JPanel(), gbc)

        add(JBScrollPane(contentPanel).apply { border = null }, BorderLayout.CENTER)

        state.addViewStateListener(onViewStateChanged)
    }

    private fun addRow(panel: JPanel, gbc: GridBagConstraints, row: Int, label: String, valueLabel: JBLabel) {
        gbc.gridy = row
        gbc.gridx = 0
        gbc.weightx = 0.0
        panel.add(JBLabel("$label:").apply {
            foreground = JBUI.CurrentTheme.Label.disabledForeground()
        }, gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(valueLabel, gbc)
    }

    private fun update(viewState: ViewState.WorkflowDetailsView) {
        val details = viewState.details
        val workflow = viewState.workflow

        val status = details?.status ?: workflow.status
        statusLabel.text = status.displayName
        statusLabel.icon = WorkflowStatusPresentation.getIcon(status)
        typeLabel.text = details?.type ?: workflow.type
        taskQueueLabel.text = details?.taskQueue ?: "-"
        runIdLabel.text = details?.runId ?: workflow.runId
        startTimeLabel.text = FormatUtils.formatDateTime(details?.startTime ?: workflow.startTime)
        closeTimeLabel.text = (details?.closeTime ?: workflow.endTime)?.let { FormatUtils.formatDateTime(it) } ?: "-"
        durationLabel.text = details?.executionDuration?.let { FormatUtils.formatDuration(it) } ?: "-"
    }

    override fun dispose() {
        state.removeViewStateListener(onViewStateChanged)
    }
}
