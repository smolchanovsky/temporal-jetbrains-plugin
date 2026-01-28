package com.github.smolchanovsky.temporalplugin.usecase

import com.github.smolchanovsky.temporalplugin.cli.TerminateWorkflowCommand
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateReader
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler

data class TerminateWorkflowRequest(
    val workflowId: String,
    val runId: String,
    val reason: String?
) : Request<Result<Unit>>

class TerminateWorkflowHandler(
    private val state: TemporalStateReader,
    private val mediatorProvider: () -> Mediator
) : RequestHandler<TerminateWorkflowRequest, Result<Unit>> {

    override suspend fun handle(request: TerminateWorkflowRequest): Result<Unit> {
        val connectionState = state.connectionState
        if (connectionState !is ConnectionState.Connected) {
            return Result.failure(IllegalStateException("Not connected"))
        }

        val result = mediatorProvider().send(
            TerminateWorkflowCommand(
                workflowId = request.workflowId,
                runId = request.runId,
                reason = request.reason,
                environment = connectionState.environment,
                namespace = connectionState.namespace
            )
        )

        if (result.isSuccess) {
            mediatorProvider().send(RefreshUseCase)
        }

        return result
    }
}
