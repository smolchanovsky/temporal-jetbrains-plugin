package com.github.smolchanovsky.temporalplugin.usecase

import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.domain.Namespace
import com.github.smolchanovsky.temporalplugin.domain.Workflow
import com.github.smolchanovsky.temporalplugin.cli.GetWorkflowsQuery
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateWriter
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler

data class ConnectUseCase(
    val environment: Environment,
    val namespace: Namespace
) : Request<Result<List<Workflow>>>

class ConnectUseCaseHandler(
    private val state: TemporalStateWriter,
    private val mediatorProvider: () -> Mediator
) : RequestHandler<ConnectUseCase, Result<List<Workflow>>> {

    override suspend fun handle(request: ConnectUseCase): Result<List<Workflow>> {
        state.updateConnectionState(ConnectionState.Connecting(request.namespace.name))

        val result = mediatorProvider().send(GetWorkflowsQuery(request.environment, request.namespace))

        result.onSuccess { workflows ->
            state.updateConnectionState(ConnectionState.Connected(request.environment, request.namespace))
            state.updateWorkflows(workflows)
        }.onFailure {
            state.updateConnectionState(ConnectionState.Disconnected)
        }

        return result
    }
}
