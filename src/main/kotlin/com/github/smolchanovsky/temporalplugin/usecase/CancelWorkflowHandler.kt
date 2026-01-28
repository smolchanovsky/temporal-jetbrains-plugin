package com.github.smolchanovsky.temporalplugin.usecase

import com.github.smolchanovsky.temporalplugin.cli.CancelWorkflowCommand
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateReader
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler

data class CancelWorkflowRequest(
    val workflowId: String,
    val runId: String
) : Request<Result<Unit>>

class CancelWorkflowHandler(
    private val state: TemporalStateReader,
    private val mediatorProvider: () -> Mediator
) : RequestHandler<CancelWorkflowRequest, Result<Unit>> {

    override suspend fun handle(request: CancelWorkflowRequest): Result<Unit> {
        val connectionState = state.connectionState
        if (connectionState !is ConnectionState.Connected) {
            return Result.failure(IllegalStateException("Not connected"))
        }

        val result = mediatorProvider().send(
            CancelWorkflowCommand(
                workflowId = request.workflowId,
                runId = request.runId,
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
