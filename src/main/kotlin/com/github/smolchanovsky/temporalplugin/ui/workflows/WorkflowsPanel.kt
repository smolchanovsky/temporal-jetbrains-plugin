package com.github.smolchanovsky.temporalplugin.ui.workflows

import com.github.smolchanovsky.temporalplugin.domain.Workflow
import com.github.smolchanovsky.temporalplugin.ui.settings.TemporalSettings
import com.github.smolchanovsky.temporalplugin.services.AutoRefreshService
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.awt.BorderLayout
import javax.swing.JPanel

class WorkflowsPanel(
    project: Project,
    scope: CoroutineScope? = null,
    onWorkflowDoubleClick: ((Workflow) -> Unit)? = null
) : JPanel(BorderLayout()), Disposable {

    @Suppress("unused") private val autoRefreshService = project.service<AutoRefreshService>()

    private val ownScope = scope == null
    private val effectiveScope = scope ?: CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val toolbar = WorkflowsToolbar(project, effectiveScope)
    private val table = WorkflowList(project, onWorkflowDoubleClick)
    private val statusLabel = ConnectionInfoLabel(project)

    init {
        if (scope == null) {
            TemporalSettings.getInstance(project).ensureCliPathDetected()
        }

        Disposer.register(this, toolbar)
        Disposer.register(this, table)
        Disposer.register(this, statusLabel)

        add(toolbar, BorderLayout.NORTH)
        add(table, BorderLayout.CENTER)
        add(statusLabel, BorderLayout.SOUTH)

        toolbar.load()
    }

    override fun dispose() {
        if (ownScope) {
            effectiveScope.cancel()
        }
    }
}
