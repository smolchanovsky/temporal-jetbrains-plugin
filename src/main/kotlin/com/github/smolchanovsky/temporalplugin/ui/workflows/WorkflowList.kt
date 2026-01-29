package com.github.smolchanovsky.temporalplugin.ui.workflows

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.domain.WorkflowStatus
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.github.smolchanovsky.temporalplugin.ui.common.FormatUtils
import com.github.smolchanovsky.temporalplugin.ui.common.WorkflowStatusPresentation
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

class WorkflowList(
    project: Project
) : JBScrollPane(), Disposable {

    private val state = project.service<TemporalState>()
    private val model = WorkflowTableModel()
    private val table = JBTable(model)

    private val onWorkflowsUpdated: (List<com.github.smolchanovsky.temporalplugin.domain.Workflow>) -> Unit = { workflows ->
        SwingUtilities.invokeLater {
            model.updateWorkflows(workflows)
            restoreSelection(workflows)
        }
    }

    private fun restoreSelection(workflows: List<com.github.smolchanovsky.temporalplugin.domain.Workflow>) {
        val selectedRunId = state.selectedWorkflowRunId ?: return
        val index = workflows.indexOfFirst { it.runId == selectedRunId }
        if (index >= 0) {
            table.setRowSelectionInterval(index, index)
        }
    }

    init {
        state.addWorkflowsListener(onWorkflowsUpdated)

        setViewportView(table)
        table.fillsViewportHeight = true
        table.autoResizeMode = JBTable.AUTO_RESIZE_ALL_COLUMNS
        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        table.setCellSelectionEnabled(false)
        table.setRowSelectionAllowed(true)
        table.setDefaultRenderer(Any::class.java, NoFocusRenderer())

        val statusColumn = table.columnModel.getColumn(STATUS_COLUMN)
        statusColumn.cellRenderer = StatusRenderer()
        statusColumn.preferredWidth = 60
        statusColumn.maxWidth = 60

        val startTimeColumn = table.columnModel.getColumn(START_TIME_COLUMN)
        startTimeColumn.preferredWidth = 100
        startTimeColumn.maxWidth = 120

        val endTimeColumn = table.columnModel.getColumn(END_TIME_COLUMN)
        endTimeColumn.preferredWidth = 100
        endTimeColumn.maxWidth = 120

        table.selectionModel.addListSelectionListener { e ->
            if (!e.valueIsAdjusting && table.selectedRow >= 0) {
                state.selectedWorkflowRunId = model.getWorkflowAt(table.selectedRow)?.runId
            }
        }

        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent): Boolean {
                val row = table.selectedRow
                if (row >= 0) {
                    model.getWorkflowAt(row)?.let { workflow ->
                        state.updateViewState(ViewState.WorkflowDetailsView(workflow, isLoading = true))
                    }
                    return true
                }
                return false
            }
        }.installOn(table)
    }

    override fun dispose() {
        state.removeWorkflowsListener(onWorkflowsUpdated)
    }

    companion object {
        private const val STATUS_COLUMN = 0
        private const val START_TIME_COLUMN = 4
        private const val END_TIME_COLUMN = 5
    }
}

private class WorkflowTableModel : AbstractTableModel() {

    private val columns = listOf(
        TextBundle.message("table.column.status"),
        TextBundle.message("table.column.workflowId"),
        TextBundle.message("table.column.runId"),
        TextBundle.message("table.column.type"),
        TextBundle.message("table.column.startTime"),
        TextBundle.message("table.column.endTime")
    )

    private var workflows: List<com.github.smolchanovsky.temporalplugin.domain.Workflow> = emptyList()

    fun updateWorkflows(newWorkflows: List<com.github.smolchanovsky.temporalplugin.domain.Workflow>) {
        workflows = newWorkflows
        fireTableDataChanged()
    }

    fun getWorkflowAt(row: Int): com.github.smolchanovsky.temporalplugin.domain.Workflow? = workflows.getOrNull(row)

    override fun getRowCount(): Int = workflows.size
    override fun getColumnCount(): Int = columns.size
    override fun getColumnName(column: Int): String = columns[column]

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        val workflow = workflows.getOrNull(rowIndex) ?: return null
        return when (columnIndex) {
            0 -> workflow.status
            1 -> workflow.id
            2 -> workflow.runId
            3 -> workflow.type
            4 -> FormatUtils.formatTime(workflow.startTime)
            5 -> workflow.endTime?.let { FormatUtils.formatTime(it) } ?: ""
            else -> null
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = false
}

private class NoFocusRenderer : DefaultTableCellRenderer() {

    override fun getTableCellRendererComponent(
        table: JTable?, value: Any?, isSelected: Boolean,
        hasFocus: Boolean, row: Int, column: Int
    ): Component {
        return super.getTableCellRendererComponent(table, value, isSelected, false, row, column)
    }
}

private class StatusRenderer : DefaultTableCellRenderer() {

    override fun getTableCellRendererComponent(
        table: JTable?, value: Any?, isSelected: Boolean,
        hasFocus: Boolean, row: Int, column: Int
    ): Component {
        super.getTableCellRendererComponent(table, value, isSelected, false, row, column)
        val status = value as? WorkflowStatus ?: WorkflowStatus.UNKNOWN
        icon = WorkflowStatusPresentation.getIcon(status)
        text = ""
        horizontalAlignment = CENTER
        toolTipText = status.displayName
        return this
    }
}
