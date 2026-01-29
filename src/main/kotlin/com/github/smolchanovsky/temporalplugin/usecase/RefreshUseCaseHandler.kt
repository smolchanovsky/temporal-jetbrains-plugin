package com.github.smolchanovsky.temporalplugin.usecase

import com.github.smolchanovsky.temporalplugin.domain.Workflow
import com.github.smolchanovsky.temporalplugin.cli.GetWorkflowsQuery
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateWriter
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler
import kotlinx.coroutines.delay

object RefreshUseCase : Request<Result<List<Workflow>>>

class RefreshUseCaseHandler(
    private val state: TemporalStateWriter,
    private val mediatorProvider: () -> Mediator
) : RequestHandler<RefreshUseCase, Result<List<Workflow>>> {

    override suspend fun handle(request: RefreshUseCase): Result<List<Workflow>> {
        val currentState = state.connectionState
        if (currentState !is ConnectionState.Connected && currentState !is ConnectionState.Refreshing) {
            return Result.failure(IllegalStateException("Not connected"))
        }

        val env = state.selectedEnvironment
        val ns = state.selectedNamespace
        val status = state.filterStatus
        val searchPrefix = state.searchQuery.takeIf { it.isNotBlank() }

        state.updateConnectionState(ConnectionState.Refreshing(env, ns))

        val result = mediatorProvider().send(GetWorkflowsQuery(env, ns, status, searchPrefix))

        result.onSuccess { workflows ->
            state.updateWorkflows(workflows)
        }

        delay(500)
        if (state.connectionState is ConnectionState.Refreshing) {
            state.updateConnectionState(ConnectionState.Connected(env, ns))
        }

        return result
    }
}
