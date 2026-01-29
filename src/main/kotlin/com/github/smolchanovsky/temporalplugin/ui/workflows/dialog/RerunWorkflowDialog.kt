package com.github.smolchanovsky.temporalplugin.ui.workflows.dialog

import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.ui.analytics.base.TrackedDialog
import com.github.smolchanovsky.temporalplugin.ui.common.AutoCompleteTextField
import com.github.smolchanovsky.temporalplugin.ui.common.onFailureNotify
import com.github.smolchanovsky.temporalplugin.usecase.GenerateWorkflowDataResult
import com.github.smolchanovsky.temporalplugin.usecase.RerunWorkflowRequest
import com.intellij.json.JsonLanguage
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.intellij.icons.AllIcons
import java.awt.Dimension
import javax.swing.JComponent

class RerunWorkflowDialog(
    private val project: Project,
    private val scope: CoroutineScope,
    data: GenerateWorkflowDataResult? = null
) : TrackedDialog(analyticsName = if (data != null) "rerun_workflow" else "run_workflow", project = project) {

    private val state = project.service<TemporalState>()
    private val mediator = project.service<TemporalMediator>().mediator
    private val json = Json { ignoreUnknownKeys = true }

    private val workflowIdField = AutoCompleteTextField.create(
        project,
        state.workflows.map { it.id }.distinct().sorted(),
        data?.workflowId ?: ""
    )
    private val workflowTypeField = AutoCompleteTextField.create(
        project,
        state.workflows.map { it.type }.distinct().sorted(),
        data?.workflowType ?: ""
    )
    private val taskQueueField = AutoCompleteTextField.create(
        project,
        state.workflows.map { it.taskQueue }.distinct().sorted(),
        data?.taskQueue ?: ""
    )
    private val inputEditor = object : LanguageTextField(
        JsonLanguage.INSTANCE,
        project,
        data?.input ?: "",
        false  // oneLineMode = false for Enter
    ) {
        override fun createEditor(): EditorEx {
            return super.createEditor().apply {
                setVerticalScrollbarVisible(true)
                setHorizontalScrollbarVisible(true)
                settings.isLineNumbersShown = true
                settings.isFoldingOutlineShown = true
            }
        }
    }.apply {
        preferredSize = Dimension(500, 200)
    }

    init {
        title = if (data != null) TextBundle.message("dialog.rerun.title") else TextBundle.message("dialog.run.title")
        setOKButtonText(TextBundle.message("dialog.run.start"))
        setOKButtonIcon(AllIcons.Actions.Execute)
        trackOpen()
        setupFieldTracking()
        init()
        // Disable Enter triggering OK when focus is in multi-line editor
        rootPane?.defaultButton = null
    }

    private fun setupFieldTracking() {
        fieldTracker.track(workflowIdField, "workflow_id")
        fieldTracker.track(taskQueueField, "task_queue")
        fieldTracker.track(inputEditor, "input")
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row(TextBundle.message("dialog.rerun.workflowId")) {
                cell(workflowIdField)
                    .align(AlignX.FILL)
                    .resizableColumn()
            }
            row(TextBundle.message("dialog.rerun.workflowType")) {
                cell(workflowTypeField)
                    .align(AlignX.FILL)
                    .resizableColumn()
            }
            row(TextBundle.message("dialog.rerun.taskQueue")) {
                cell(taskQueueField)
                    .align(AlignX.FILL)
                    .resizableColumn()
            }
            row {
                cell(JBLabel(TextBundle.message("dialog.rerun.input")))
                    .align(AlignY.TOP)
            }
            row {
                cell(inputEditor)
                    .align(AlignX.FILL)
                    .align(AlignY.FILL)
                    .resizableColumn()
            }.resizableRow()
        }
    }

    override fun doValidate(): ValidationInfo? {
        if (workflowIdField.text.isBlank()) {
            return ValidationInfo(
                TextBundle.message("dialog.rerun.validation.workflowIdRequired"),
                workflowIdField
            )
        }
        if (workflowTypeField.text.isBlank()) {
            return ValidationInfo(
                TextBundle.message("dialog.rerun.validation.workflowTypeRequired"),
                workflowTypeField
            )
        }
        if (taskQueueField.text.isBlank()) {
            return ValidationInfo(
                TextBundle.message("dialog.rerun.validation.taskQueueRequired"),
                taskQueueField
            )
        }
        if (inputEditor.text.isNotBlank()) {
            try {
                json.parseToJsonElement(inputEditor.text)
            } catch (e: Exception) {
                return ValidationInfo(
                    TextBundle.message("dialog.rerun.validation.invalidJson"),
                    inputEditor
                )
            }
        }
        return null
    }

    override fun doOKAction() {
        scope.launch {
            mediator.send(
                RerunWorkflowRequest(
                    workflowId = workflowIdField.text.trim() ?: "",
                    workflowType = workflowTypeField.text.trim() ?: "",
                    taskQueue = taskQueueField.text.trim() ?: "",
                    input = inputEditor.text.trim().takeIf { it.isNotBlank() }
                )
            ).onFailureNotify(project)
        }
        super.doOKAction()
    }
}
