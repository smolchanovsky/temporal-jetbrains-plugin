package com.github.smolchanovsky.temporalplugin.ui.navigation

import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch

sealed class NavigationResult {
    data class SingleMatch(val match: WorkflowMatch) : NavigationResult()
    data class MultipleMatches(val title: String, val matches: List<WorkflowMatch>) : NavigationResult()
    data class NotFound(val workflowType: String) : NavigationResult()
}
