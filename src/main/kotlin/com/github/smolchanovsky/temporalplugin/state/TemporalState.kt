package com.github.smolchanovsky.temporalplugin.state

import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.domain.Namespace
import com.github.smolchanovsky.temporalplugin.domain.Workflow
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.thisLogger
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.SwingUtilities

@Service(Service.Level.PROJECT)
@State(name = "TemporalState", storages = [Storage("temporal.xml")])
class TemporalState : TemporalStateWriter, PersistentStateComponent<TemporalState.PersistentState>, Disposable {

    data class PersistentState(
        var selectedEnvironment: String = Environment.LOCAL.name,
        var selectedNamespace: String = Namespace.DEFAULT.name
    )

    private var persistentState = PersistentState()

    private val cliAvailableListeners = CopyOnWriteArrayList<(Boolean) -> Unit>()
    private val connectionStateListeners = CopyOnWriteArrayList<(ConnectionState) -> Unit>()
    private val workflowsListeners = CopyOnWriteArrayList<(List<Workflow>) -> Unit>()
    private val environmentListeners = CopyOnWriteArrayList<() -> Unit>()
    private val namespaceListeners = CopyOnWriteArrayList<() -> Unit>()
    private val viewStateListeners = CopyOnWriteArrayList<(ViewState) -> Unit>()

    // CLI availability
    @Volatile
    private var _cliAvailable: Boolean = false
    override val cliAvailable: Boolean
        get() = _cliAvailable

    override fun updateCliAvailable(available: Boolean) {
        if (_cliAvailable != available) {
            thisLogger().info("cliAvailable: $_cliAvailable -> $available")
            _cliAvailable = available
            if (SwingUtilities.isEventDispatchThread()) {
                cliAvailableListeners.forEach { it(available) }
            } else {
                SwingUtilities.invokeLater { cliAvailableListeners.forEach { it(available) } }
            }
        }
    }

    // Connection state
    @Volatile
    private var _connectionState: ConnectionState = ConnectionState.Disconnected
    override var connectionState: ConnectionState
        get() = _connectionState
        private set(value) {
            _connectionState = value
            notifyConnectionStateListeners(value)
        }

    // Workflows
    @Volatile
    private var _workflows: List<Workflow> = emptyList()
    override var workflows: List<Workflow>
        get() = _workflows
        private set(value) {
            _workflows = value
            notifyWorkflowsListeners(value)
        }

    override fun updateConnectionState(state: ConnectionState) {
        thisLogger().info("connectionState: $_connectionState -> $state")
        _connectionState = state
        if (SwingUtilities.isEventDispatchThread()) {
            notifyConnectionStateListeners(state)
        } else {
            SwingUtilities.invokeLater { notifyConnectionStateListeners(state) }
        }
    }

    override fun updateWorkflows(workflows: List<Workflow>) {
        thisLogger().info("workflows: ${_workflows.size} -> ${workflows.size}")
        _workflows = workflows
        if (SwingUtilities.isEventDispatchThread()) {
            notifyWorkflowsListeners(workflows)
        } else {
            SwingUtilities.invokeLater { notifyWorkflowsListeners(workflows) }
        }
    }

    private fun notifyConnectionStateListeners(state: ConnectionState) {
        connectionStateListeners.forEach { it(state) }
    }

    private fun notifyWorkflowsListeners(workflows: List<Workflow>) {
        workflowsListeners.forEach { it(workflows) }
    }

    // View state
    @Volatile
    private var _viewState: ViewState = ViewState.WorkflowList
    override val viewState: ViewState
        get() = _viewState

    override fun updateViewState(viewState: ViewState) {
        thisLogger().info("viewState: $_viewState -> $viewState")
        _viewState = viewState
        if (SwingUtilities.isEventDispatchThread()) {
            notifyViewStateListeners(viewState)
        } else {
            SwingUtilities.invokeLater { notifyViewStateListeners(viewState) }
        }
    }

    private fun notifyViewStateListeners(viewState: ViewState) {
        viewStateListeners.forEach { it(viewState) }
    }

    override var selectedEnvironment: Environment
        get() = Environment(persistentState.selectedEnvironment)
        set(value) {
            thisLogger().info("selectedEnvironment: ${persistentState.selectedEnvironment} -> ${value.name}")
            if (persistentState.selectedEnvironment != value.name) {
                persistentState.selectedEnvironment = value.name
                environmentListeners.forEach { it() }
            }
        }

    override var selectedNamespace: Namespace
        get() = Namespace(persistentState.selectedNamespace)
        set(value) {
            thisLogger().info("selectedNamespace: ${persistentState.selectedNamespace} -> ${value.name}")
            if (persistentState.selectedNamespace != value.name) {
                persistentState.selectedNamespace = value.name
                namespaceListeners.forEach { it() }
            }
        }


    override var selectedWorkflowRunId: String? = null

    override val selectedWorkflow: Workflow?
        get() = selectedWorkflowRunId?.let { runId -> workflows.find { it.runId == runId } }


    override fun addCliAvailableListener(listener: (Boolean) -> Unit) { cliAvailableListeners.add(listener) }
    override fun removeCliAvailableListener(listener: (Boolean) -> Unit) { cliAvailableListeners.remove(listener) }

    override fun addConnectionStateListener(listener: (ConnectionState) -> Unit) { connectionStateListeners.add(listener) }
    override fun removeConnectionStateListener(listener: (ConnectionState) -> Unit) { connectionStateListeners.remove(listener) }

    override fun addWorkflowsListener(listener: (List<Workflow>) -> Unit) { workflowsListeners.add(listener) }
    override fun removeWorkflowsListener(listener: (List<Workflow>) -> Unit) { workflowsListeners.remove(listener) }

    override fun addEnvironmentListener(listener: () -> Unit) { environmentListeners.add(listener) }
    override fun removeEnvironmentListener(listener: () -> Unit) { environmentListeners.remove(listener) }

    override fun addNamespaceListener(listener: () -> Unit) { namespaceListeners.add(listener) }
    override fun removeNamespaceListener(listener: () -> Unit) { namespaceListeners.remove(listener) }

    override fun addViewStateListener(listener: (ViewState) -> Unit) { viewStateListeners.add(listener) }
    override fun removeViewStateListener(listener: (ViewState) -> Unit) { viewStateListeners.remove(listener) }


    override fun getState(): PersistentState = persistentState

    override fun loadState(state: PersistentState) {
        thisLogger().info("loadState: env=${state.selectedEnvironment}, ns=${state.selectedNamespace}")
        persistentState = state
    }

    override fun dispose() {
        cliAvailableListeners.clear()
        connectionStateListeners.clear()
        workflowsListeners.clear()
        environmentListeners.clear()
        namespaceListeners.clear()
        viewStateListeners.clear()
    }
}
