package com.github.smolchanovsky.temporalplugin.state

import com.github.smolchanovsky.temporalplugin.domain.Workflow
import com.github.smolchanovsky.temporalplugin.domain.WorkflowDetails
import com.github.smolchanovsky.temporalplugin.domain.WorkflowHistory

sealed class ViewState {
    data object WorkflowList : ViewState()

    data class WorkflowDetailsView(
        val workflow: Workflow,
        val details: WorkflowDetails? = null,
        val history: WorkflowHistory? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : ViewState()
}
