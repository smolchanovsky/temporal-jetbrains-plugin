package com.github.smolchanovsky.temporalplugin.ui.workflows.dialog

import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.domain.Workflow
import com.github.smolchanovsky.temporalplugin.ui.analytics.base.TrackedDialog
import com.github.smolchanovsky.temporalplugin.ui.common.onFailureNotify
import com.github.smolchanovsky.temporalplugin.usecase.TerminateWorkflowRequest
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.swing.JComponent

class TerminateWorkflowDialog(
    private val project: Project,
    private val scope: CoroutineScope,
    private val workflow: Workflow
) : TrackedDialog(analyticsName = "terminate_workflow", project = project) {

    private val mediator = project.service<TemporalMediator>().mediator

    private val reasonField = JBTextField().apply {
        columns = 40
    }

    init {
        title = TextBundle.message("dialog.terminate.title")
        setOKButtonText(TextBundle.message("dialog.terminate.button"))
        trackOpen()
        setupFieldTracking()
        init()
    }

    private fun setupFieldTracking() {
        fieldTracker.track(reasonField, "reason")
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row(TextBundle.message("dialog.terminate.reason")) {
                cell(reasonField)
                    .align(AlignX.FILL)
                    .resizableColumn()
            }
        }
    }

    override fun doOKAction() {
        scope.launch {
            mediator.send(
                TerminateWorkflowRequest(
                    workflowId = workflow.id,
                    runId = workflow.runId,
                    reason = reasonField.text.trim().takeIf { it.isNotBlank() }
                )
            ).onFailureNotify(project)
        }
        super.doOKAction()
    }
}
