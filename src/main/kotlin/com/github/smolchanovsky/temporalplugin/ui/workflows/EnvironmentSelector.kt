package com.github.smolchanovsky.temporalplugin.ui.workflows

import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.TemporalMediator
import com.github.smolchanovsky.temporalplugin.cli.GetEnvironmentsQuery
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.ui.common.onFailureNotify
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import java.awt.Dimension
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.swing.JComponent
import javax.swing.SwingUtilities

class EnvironmentSelector(
    private val project: Project,
    private val scope: CoroutineScope
) : ComboBoxAction(), DumbAware {

    private val mediator = project.service<TemporalMediator>().mediator
    private val state = project.service<TemporalState>()
    private var environments: List<Environment> = listOf(Environment.LOCAL)

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val component = super.createCustomComponent(presentation, place)
        component.preferredSize = Dimension(120, component.preferredSize.height)
        component.maximumSize = Dimension(120, component.maximumSize.height)
        return component
    }

    override fun createPopupActionGroup(button: JComponent, dataContext: DataContext): DefaultActionGroup {
        val group = DefaultActionGroup()
        environments.forEach { env ->
            group.add(SelectEnvironmentAction(env))
        }
        return group
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = state.cliAvailable
        e.presentation.text = "Env: ${state.selectedEnvironment.name}"
    }

    fun load() {
        scope.launch {
            mediator.send(GetEnvironmentsQuery)
                .onFailureNotify(project)
                .onSuccess { envList ->
                    state.updateCliAvailable(true)
                    SwingUtilities.invokeLater {
                        environments = listOf(Environment.LOCAL) + envList
                    }
                }
                .onFailure {
                    state.updateCliAvailable(false)
                }
        }
    }

    private inner class SelectEnvironmentAction(
        private val environment: Environment
    ) : AnAction(environment.name) {

        override fun getActionUpdateThread() = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            state.selectedEnvironment = environment
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = environment != state.selectedEnvironment
        }
    }
}
