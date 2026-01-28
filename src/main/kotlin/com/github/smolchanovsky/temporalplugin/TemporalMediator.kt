package com.github.smolchanovsky.temporalplugin

import com.github.smolchanovsky.temporalplugin.cli.CancelWorkflowCommandHandler
import com.github.smolchanovsky.temporalplugin.cli.CheckServerHealthQueryHandler
import com.github.smolchanovsky.temporalplugin.cli.GetCliVersionQueryHandler
import com.github.smolchanovsky.temporalplugin.cli.GetEnvironmentsQueryHandler
import com.github.smolchanovsky.temporalplugin.cli.GetNamespacesQueryHandler
import com.github.smolchanovsky.temporalplugin.cli.GetWorkflowDetailsQueryHandler
import com.github.smolchanovsky.temporalplugin.cli.GetWorkflowHistoryQueryHandler
import com.github.smolchanovsky.temporalplugin.cli.GetWorkflowsQueryHandler
import com.github.smolchanovsky.temporalplugin.cli.StartWorkflowCommandHandler
import com.github.smolchanovsky.temporalplugin.cli.TerminateWorkflowCommandHandler
import com.github.smolchanovsky.temporalplugin.cli.utils.CliExecutor
import com.github.smolchanovsky.temporalplugin.ui.settings.TemporalSettings
import com.github.smolchanovsky.temporalplugin.state.TemporalState
import com.github.smolchanovsky.temporalplugin.usecase.CancelWorkflowHandler
import com.github.smolchanovsky.temporalplugin.usecase.ConnectUseCaseHandler
import com.github.smolchanovsky.temporalplugin.usecase.DisconnectUseCaseHandler
import com.github.smolchanovsky.temporalplugin.usecase.GenerateWorkflowDataHandler
import com.github.smolchanovsky.temporalplugin.usecase.LoadWorkflowDetailsUseCaseHandler
import com.github.smolchanovsky.temporalplugin.usecase.RefreshUseCaseHandler
import com.github.smolchanovsky.temporalplugin.usecase.RunSimilarWorkflowHandler
import com.github.smolchanovsky.temporalplugin.usecase.TerminateWorkflowHandler
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.trendyol.kediatr.HandlerRegistryProvider
import com.trendyol.kediatr.Mediator

@Service(Service.Level.PROJECT)
class TemporalMediator(project: Project) {

    private val state = project.service<TemporalState>()
    private val cli = CliExecutor(TemporalSettings.Companion.getInstance(project))

    val mediator: Mediator by lazy {
        lateinit var instance: Mediator
        instance = HandlerRegistryProvider.Companion.createMediator(
            handlers = listOf(
                CheckServerHealthQueryHandler(cli),
                GetCliVersionQueryHandler(cli),
                GetEnvironmentsQueryHandler(cli),
                GetNamespacesQueryHandler(cli),
                GetWorkflowsQueryHandler(cli),
                GetWorkflowDetailsQueryHandler(cli),
                GetWorkflowHistoryQueryHandler(cli),
                StartWorkflowCommandHandler(cli),
                CancelWorkflowCommandHandler(cli),
                TerminateWorkflowCommandHandler(cli),
                ConnectUseCaseHandler(state) { instance },
                DisconnectUseCaseHandler(state),
                RefreshUseCaseHandler(state) { instance },
                LoadWorkflowDetailsUseCaseHandler(state) { instance },
                GenerateWorkflowDataHandler(state) { instance },
                RunSimilarWorkflowHandler(state) { instance },
                CancelWorkflowHandler(state) { instance },
                TerminateWorkflowHandler(state) { instance }
            )
        )
        instance
    }
}