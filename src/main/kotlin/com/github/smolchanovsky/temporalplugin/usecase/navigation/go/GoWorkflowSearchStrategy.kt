package com.github.smolchanovsky.temporalplugin.usecase.navigation.go

import com.github.smolchanovsky.temporalplugin.usecase.navigation.WorkflowMatch
import com.goide.psi.GoFile

interface GoWorkflowSearchStrategy {
    fun findMatches(goFile: GoFile, workflowType: String?): List<WorkflowMatch>
}
