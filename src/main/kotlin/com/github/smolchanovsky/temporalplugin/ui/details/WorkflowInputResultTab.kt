package com.github.smolchanovsky.temporalplugin.ui.details

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.github.smolchanovsky.temporalplugin.ui.common.JsonViewerPanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBSplitter
import com.intellij.ui.TitledSeparator
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.SwingUtilities

class WorkflowInputResultTab(project: Project) : JPanel(BorderLayout()), Disposable {

    private val state = project.service<TemporalState>()

    private val inputPanel = JsonViewerPanel(project)
    private val resultPanel = JsonViewerPanel(project)

    private val onViewStateChanged: (ViewState) -> Unit = { viewState ->
        if (viewState is ViewState.WorkflowDetailsView) {
            SwingUtilities.invokeLater {
                inputPanel.setJson(viewState.history?.input)
                resultPanel.setJson(viewState.history?.result)
            }
        }
    }

    init {
        Disposer.register(this, inputPanel)
        Disposer.register(this, resultPanel)

        val splitter = JBSplitter(false, 0.5f)
        splitter.firstComponent = createTitledPanel(TextBundle.message("details.input"), inputPanel)
        splitter.secondComponent = createTitledPanel(TextBundle.message("details.result"), resultPanel)

        add(splitter, BorderLayout.CENTER)

        state.addViewStateListener(onViewStateChanged)
    }

    private fun createTitledPanel(title: String, content: JPanel): JPanel {
        return JPanel(BorderLayout()).apply {
            add(TitledSeparator(title), BorderLayout.NORTH)
            add(content, BorderLayout.CENTER)
        }
    }

    override fun dispose() {
        state.removeViewStateListener(onViewStateChanged)
    }
}
