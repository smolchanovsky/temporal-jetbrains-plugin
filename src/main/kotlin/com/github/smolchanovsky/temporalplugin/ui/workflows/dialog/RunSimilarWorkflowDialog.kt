package com.github.smolchanovsky.temporalplugin.ui.workflows.dialog

import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.ui.common.onFailureNotify
import com.github.smolchanovsky.temporalplugin.usecase.GenerateWorkflowDataResult
import com.github.smolchanovsky.temporalplugin.usecase.RunSimilarWorkflowRequest
import com.intellij.json.JsonLanguage
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.awt.Dimension
import javax.swing.JComponent

class RunSimilarWorkflowDialog(
    private val project: Project,
    private val scope: CoroutineScope,
    data: GenerateWorkflowDataResult
) : DialogWrapper(project) {

    private val mediator = project.service<TemporalMediator>().mediator
    private val json = Json { ignoreUnknownKeys = true }

    private val workflowIdField = JBTextField(data.workflowId)
    private val workflowTypeField = JBTextField(data.workflowType)
    private val taskQueueField = JBTextField(data.taskQueue)
    private val inputEditor = object : LanguageTextField(JsonLanguage.INSTANCE, project, data.input) {
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
        title = TextBundle.message("dialog.runSimilar.title")
        setOKButtonText(TextBundle.message("dialog.runSimilar.start"))
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row(TextBundle.message("dialog.runSimilar.workflowId")) {
                cell(workflowIdField)
                    .align(AlignX.FILL)
                    .resizableColumn()
            }
            row(TextBundle.message("dialog.runSimilar.workflowType")) {
                cell(workflowTypeField)
                    .align(AlignX.FILL)
                    .resizableColumn()
            }
            row(TextBundle.message("dialog.runSimilar.taskQueue")) {
                cell(taskQueueField)
                    .align(AlignX.FILL)
                    .resizableColumn()
            }
            row {
                cell(JBLabel(TextBundle.message("dialog.runSimilar.input")))
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
                TextBundle.message("dialog.runSimilar.validation.workflowIdRequired"),
                workflowIdField
            )
        }
        if (workflowTypeField.text.isBlank()) {
            return ValidationInfo(
                TextBundle.message("dialog.runSimilar.validation.workflowTypeRequired"),
                workflowTypeField
            )
        }
        if (taskQueueField.text.isBlank()) {
            return ValidationInfo(
                TextBundle.message("dialog.runSimilar.validation.taskQueueRequired"),
                taskQueueField
            )
        }
        if (inputEditor.text.isNotBlank()) {
            try {
                json.parseToJsonElement(inputEditor.text)
            } catch (e: Exception) {
                return ValidationInfo(
                    TextBundle.message("dialog.runSimilar.validation.invalidJson"),
                    inputEditor
                )
            }
        }
        return null
    }

    override fun doOKAction() {
        scope.launch {
            mediator.send(
                RunSimilarWorkflowRequest(
                    workflowId = workflowIdField.text.trim(),
                    workflowType = workflowTypeField.text.trim(),
                    taskQueue = taskQueueField.text.trim(),
                    input = inputEditor.text.trim().takeIf { it.isNotBlank() }
                )
            ).onFailureNotify(project)
        }
        super.doOKAction()
    }
}
