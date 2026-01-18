package com.github.smolchanovsky.temporalplugin.ui.details

import com.github.smolchanovsky.temporalplugin.ui.settings.TemporalSettings
import com.github.smolchanovsky.temporalplugin.services.AutoRefreshService
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.github.smolchanovsky.temporalplugin.ui.workflows.WorkflowsPanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.awt.CardLayout
import javax.swing.JPanel
import javax.swing.SwingUtilities

class WorkflowsRootPanel(
    private val project: Project
) : JPanel(CardLayout()), Disposable {

    @Suppress("unused") private val autoRefreshService = project.service<AutoRefreshService>()

    private val state = project.service<TemporalState>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cardLayout = layout as CardLayout

    private val listPanel = WorkflowsPanel(project, scope) { workflow ->
        state.updateViewState(ViewState.WorkflowDetailsView(workflow, isLoading = true))
    }

    private val detailsPanel = WorkflowDetailsPanel(project, scope)

    private val onViewStateChanged: (ViewState) -> Unit = { viewState ->
        SwingUtilities.invokeLater {
            when (viewState) {
                is ViewState.WorkflowList -> cardLayout.show(this, CARD_LIST)
                is ViewState.WorkflowDetailsView -> cardLayout.show(this, CARD_DETAILS)
            }
        }
    }

    init {
        TemporalSettings.getInstance(project).ensureCliPathDetected()

        Disposer.register(this, listPanel)
        Disposer.register(this, detailsPanel)

        add(listPanel, CARD_LIST)
        add(detailsPanel, CARD_DETAILS)

        state.addViewStateListener(onViewStateChanged)

        // Show initial view
        cardLayout.show(this, CARD_LIST)
    }

    override fun dispose() {
        state.removeViewStateListener(onViewStateChanged)
        scope.cancel()
    }

    companion object {
        private const val CARD_LIST = "list"
        private const val CARD_DETAILS = "details"
    }
}
