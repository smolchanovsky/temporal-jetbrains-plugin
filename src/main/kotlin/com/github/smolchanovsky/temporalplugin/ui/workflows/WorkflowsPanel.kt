package com.github.smolchanovsky.temporalplugin.ui.workflows

import com.github.smolchanovsky.temporalplugin.services.AutoRefreshService
import com.github.smolchanovsky.temporalplugin.ui.analytics.AnalyticsConsentBanner
import com.github.smolchanovsky.temporalplugin.ui.settings.TemporalSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JPanel

class WorkflowsPanel(
    project: Project,
    scope: CoroutineScope? = null
) : JPanel(BorderLayout()), Disposable {

    @Suppress("unused") private val autoRefreshService = project.service<AutoRefreshService>()

    private val ownScope = scope == null
    private val effectiveScope = scope ?: CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val toolbar = WorkflowsToolbar(project, effectiveScope)
    private val table = WorkflowList(project)
    private val statusIcon = ConnectionStatusIcon(project)
    private val statusLabel = ConnectionInfoLabel(project)
    private var consentBanner: AnalyticsConsentBanner? = null

    init {
        if (scope == null) {
            TemporalSettings.getInstance(project).ensureCliPathDetected()
        }

        Disposer.register(this, toolbar)
        Disposer.register(this, table)
        Disposer.register(this, statusIcon)
        Disposer.register(this, statusLabel)

        val topPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(toolbar)
            if (AnalyticsConsentBanner.shouldShow()) {
                consentBanner = AnalyticsConsentBanner { dismissConsentBanner() }
                add(consentBanner)
            }
        }

        val bottomPanel = JPanel(BorderLayout()).apply {
            add(statusIcon, BorderLayout.WEST)
            add(statusLabel, BorderLayout.CENTER)
        }

        add(topPanel, BorderLayout.NORTH)
        add(table, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)

        toolbar.load()
    }

    private fun dismissConsentBanner() {
        consentBanner?.let { banner ->
            banner.parent?.remove(banner)
            consentBanner = null
            revalidate()
            repaint()
        }
    }

    override fun dispose() {
        if (ownScope) {
            effectiveScope.cancel()
        }
    }
}
