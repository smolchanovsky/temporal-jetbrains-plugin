package com.github.smolchanovsky.temporalplugin.ui.details

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBSplitter
import java.awt.BorderLayout
import javax.swing.JPanel

class WorkflowOverviewTab(project: Project) : JPanel(BorderLayout()), Disposable {

    private val eventHistoryList = EventHistoryList(project)
    private val metadataPanel = MetadataPanel(project)

    init {
        Disposer.register(this, eventHistoryList)
        Disposer.register(this, metadataPanel)

        val splitter = JBSplitter(false, 0.6f)
        splitter.firstComponent = eventHistoryList
        splitter.secondComponent = metadataPanel

        add(splitter, BorderLayout.CENTER)
    }

    override fun dispose() {}
}
