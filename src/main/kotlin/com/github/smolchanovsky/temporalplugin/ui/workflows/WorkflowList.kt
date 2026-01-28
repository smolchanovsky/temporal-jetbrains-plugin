package com.github.smolchanovsky.temporalplugin.ui.workflows

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.domain.Workflow
import com.github.smolchanovsky.temporalplugin.domain.WorkflowStatus
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.ui.common.FormatUtils
import com.github.smolchanovsky.temporalplugin.ui.common.WorkflowStatusPresentation
import com.intellij.icons.AllIcons
import com.intellij.ide.CopyProvider
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.Component
import java.awt.Cursor
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

class WorkflowList(
    project: Project,
    private val onWorkflowDoubleClick: ((Workflow) -> Unit)? = null
) : JBScrollPane(), Disposable, DataProvider, CopyProvider {

    private val state = project.service<TemporalState>()
    private val model = WorkflowTableModel()
    private val table = JBTable(model)

    private val onWorkflowsUpdated: (List<Workflow>) -> Unit = { workflows ->
        SwingUtilities.invokeLater {
            model.updateWorkflows(workflows)
            restoreSelection(workflows)
        }
    }

    private fun restoreSelection(workflows: List<Workflow>) {
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
        table.cellSelectionEnabled = true

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

        val detailsColumn = table.columnModel.getColumn(DETAILS_COLUMN)
        detailsColumn.cellRenderer = DetailsRenderer()
        detailsColumn.preferredWidth = 32
        detailsColumn.maxWidth = 32
        detailsColumn.minWidth = 32

        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val column = table.columnAtPoint(e.point)
                val row = table.rowAtPoint(e.point)
                if (column == DETAILS_COLUMN && row >= 0) {
                    model.getWorkflowAt(row)?.let { onWorkflowDoubleClick?.invoke(it) }
                }
            }
        })

        table.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                val column = table.columnAtPoint(e.point)
                table.cursor = if (column == DETAILS_COLUMN) {
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                } else {
                    Cursor.getDefaultCursor()
                }
            }
        })

        table.selectionModel.addListSelectionListener { e ->
            if (!e.valueIsAdjusting && table.selectedRow >= 0) {
                state.selectedWorkflowRunId = model.getWorkflowAt(table.selectedRow)?.runId
            }
        }

        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent): Boolean {
                val row = table.selectedRow
                if (row >= 0) {
                    model.getWorkflowAt(row)?.let { onWorkflowDoubleClick?.invoke(it) }
                    return true
                }
                return false
            }
        }.installOn(table)
    }

    override fun dispose() {
        state.removeWorkflowsListener(onWorkflowsUpdated)
    }

    // DataProvider implementation
    override fun getData(dataId: String): Any? {
        if (PlatformDataKeys.COPY_PROVIDER.`is`(dataId)) {
            return this
        }
        return null
    }

    // CopyProvider implementation
    override fun isCopyEnabled(dataContext: DataContext): Boolean {
        val row = table.selectedRow
        val column = table.selectedColumn
        if (row < 0 || column < 0) return false
        val value = table.getValueAt(row, column)?.toString() ?: ""
        return value.isNotEmpty()
    }

    override fun isCopyVisible(dataContext: DataContext): Boolean = true

    override fun performCopy(dataContext: DataContext) {
        val row = table.selectedRow
        val column = table.selectedColumn
        if (row >= 0 && column >= 0) {
            val value = table.getValueAt(row, column)?.toString() ?: ""
            if (value.isNotEmpty()) {
                CopyPasteManager.getInstance().setContents(StringSelection(value))
            }
        }
    }

    companion object {
        private const val STATUS_COLUMN = 0
        private const val START_TIME_COLUMN = 4
        private const val END_TIME_COLUMN = 5
        private const val DETAILS_COLUMN = 6
    }
}

private class WorkflowTableModel : AbstractTableModel() {

    private val columns = listOf(
        TextBundle.message("table.column.status"),
        TextBundle.message("table.column.workflowId"),
        TextBundle.message("table.column.runId"),
        TextBundle.message("table.column.type"),
        TextBundle.message("table.column.startTime"),
        TextBundle.message("table.column.endTime"),
        ""
    )

    private var workflows: List<Workflow> = emptyList()

    fun updateWorkflows(newWorkflows: List<Workflow>) {
        workflows = newWorkflows
        fireTableDataChanged()
    }

    fun getWorkflowAt(row: Int): Workflow? = workflows.getOrNull(row)

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
            6 -> "" // Details column - icon only
            else -> null
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = false
}

private class StatusRenderer : DefaultTableCellRenderer() {

    override fun getTableCellRendererComponent(
        table: JTable?, value: Any?, isSelected: Boolean,
        hasFocus: Boolean, row: Int, column: Int
    ): Component {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        val status = value as? WorkflowStatus ?: WorkflowStatus.UNKNOWN
        icon = WorkflowStatusPresentation.getIcon(status)
        text = ""
        horizontalAlignment = CENTER
        toolTipText = status.displayName
        return this
    }
}

private class DetailsRenderer : DefaultTableCellRenderer() {

    override fun getTableCellRendererComponent(
        table: JTable?, value: Any?, isSelected: Boolean,
        hasFocus: Boolean, row: Int, column: Int
    ): Component {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        icon = AllIcons.Ide.External_link_arrow
        text = ""
        horizontalAlignment = CENTER
        return this
    }
}
