package com.github.smolchanovsky.temporalplugin.state

import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.domain.Namespace
import com.github.smolchanovsky.temporalplugin.domain.Workflow
import com.github.smolchanovsky.temporalplugin.domain.WorkflowStatus

interface TemporalStateReader {
    val cliAvailable: Boolean
    val connectionState: ConnectionState
    val workflows: List<Workflow>
    val selectedEnvironment: Environment
    val selectedNamespace: Namespace
    var selectedWorkflowRunId: String?
    val selectedWorkflow: Workflow?
    val viewState: ViewState
    var filterStatus: WorkflowStatus?
    var searchQuery: String

    fun addCliAvailableListener(listener: (Boolean) -> Unit)
    fun removeCliAvailableListener(listener: (Boolean) -> Unit)

    fun addConnectionStateListener(listener: (ConnectionState) -> Unit)
    fun removeConnectionStateListener(listener: (ConnectionState) -> Unit)

    fun addWorkflowsListener(listener: (List<Workflow>) -> Unit)
    fun removeWorkflowsListener(listener: (List<Workflow>) -> Unit)

    fun addEnvironmentListener(listener: () -> Unit)
    fun removeEnvironmentListener(listener: () -> Unit)

    fun addNamespaceListener(listener: () -> Unit)
    fun removeNamespaceListener(listener: () -> Unit)

    fun addViewStateListener(listener: (ViewState) -> Unit)
    fun removeViewStateListener(listener: (ViewState) -> Unit)

    fun addSelectedWorkflowListener(listener: () -> Unit)
    fun removeSelectedWorkflowListener(listener: () -> Unit)

    fun addFilterListener(listener: () -> Unit)
    fun removeFilterListener(listener: () -> Unit)
}
