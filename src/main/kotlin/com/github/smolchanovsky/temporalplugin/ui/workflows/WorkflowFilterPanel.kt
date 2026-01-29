package com.github.smolchanovsky.temporalplugin.ui.workflows

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.domain.WorkflowStatus
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.usecase.RefreshUseCase
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.event.ItemEvent
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.event.DocumentEvent

class WorkflowFilterPanel(
    private val project: Project,
    private val scope: CoroutineScope
) : JPanel(BorderLayout(JBUI.scale(4), 0)), Disposable {

    private val state = project.service<TemporalState>()
    private val mediator = project.service<TemporalMediator>().mediator

    private val searchField = SearchTextField()
    private val statusComboBox = JComboBox<StatusItem>()

    private var debounceJob: Job? = null

    private val onConnectionStateChanged: (ConnectionState) -> Unit = { connectionState ->
        val enabled = connectionState is ConnectionState.Connected ||
            connectionState is ConnectionState.Refreshing
        setFilterEnabled(enabled)
    }

    private fun setFilterEnabled(enabled: Boolean) {
        searchField.isEnabled = enabled
        searchField.textEditor.isEnabled = enabled
        statusComboBox.isEnabled = enabled
    }

    init {
        border = JBUI.Borders.empty(2, 4)
        isOpaque = false

        // Search field
        searchField.textEditor.emptyText.text = TextBundle.message("search.placeholder")
        searchField.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                debounceJob?.cancel()
                debounceJob = scope.launch {
                    delay(DEBOUNCE_MS)
                    val query = searchField.text.trim()
                    if (state.searchQuery != query) {
                        state.searchQuery = query
                        mediator.send(RefreshUseCase)
                    }
                }
            }
        })

        // Status filter
        val statuses = listOf(
            StatusItem(null, TextBundle.message("filter.status.all")),
            StatusItem(WorkflowStatus.RUNNING, WorkflowStatus.RUNNING.displayName),
            StatusItem(WorkflowStatus.COMPLETED, WorkflowStatus.COMPLETED.displayName),
            StatusItem(WorkflowStatus.FAILED, WorkflowStatus.FAILED.displayName),
            StatusItem(WorkflowStatus.CANCELED, WorkflowStatus.CANCELED.displayName),
            StatusItem(WorkflowStatus.TERMINATED, WorkflowStatus.TERMINATED.displayName),
            StatusItem(WorkflowStatus.TIMED_OUT, WorkflowStatus.TIMED_OUT.displayName)
        )
        statusComboBox.model = DefaultComboBoxModel(statuses.toTypedArray())
        statusComboBox.toolTipText = TextBundle.message("filter.status.tooltip")

        statusComboBox.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                val selected = e.item as StatusItem
                if (state.filterStatus != selected.status) {
                    state.filterStatus = selected.status
                    scope.launch {
                        mediator.send(RefreshUseCase)
                    }
                }
            }
        }

        // Layout: Search first, then status filter
        add(searchField, BorderLayout.CENTER)
        add(statusComboBox, BorderLayout.EAST)

        // Initial state
        state.addConnectionStateListener(onConnectionStateChanged)
        val connected = state.connectionState is ConnectionState.Connected ||
            state.connectionState is ConnectionState.Refreshing
        setFilterEnabled(connected)
    }

    override fun dispose() {
        debounceJob?.cancel()
        state.removeConnectionStateListener(onConnectionStateChanged)
    }

    private data class StatusItem(val status: WorkflowStatus?, val displayName: String) {
        override fun toString(): String = displayName
    }

    companion object {
        private const val DEBOUNCE_MS = 300L
    }
}
