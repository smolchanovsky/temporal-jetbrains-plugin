package com.github.smolchanovsky.temporalplugin.state

import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.domain.Namespace
import com.github.smolchanovsky.temporalplugin.domain.Workflow

interface TemporalStateReader {
    val cliAvailable: Boolean
    val connectionState: ConnectionState
    val workflows: List<Workflow>
    val selectedEnvironment: Environment
    val selectedNamespace: Namespace
    var selectedWorkflowRunId: String?
    val selectedWorkflow: Workflow?
    val viewState: ViewState

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
}
