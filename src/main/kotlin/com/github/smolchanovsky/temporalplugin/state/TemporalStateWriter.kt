package com.github.smolchanovsky.temporalplugin.state

import com.github.smolchanovsky.temporalplugin.domain.Workflow

interface TemporalStateWriter : TemporalStateReader {

    fun updateCliAvailable(available: Boolean)

    fun updateConnectionState(state: ConnectionState)

    fun updateWorkflows(workflows: List<Workflow>)

    fun updateViewState(viewState: ViewState)
}
