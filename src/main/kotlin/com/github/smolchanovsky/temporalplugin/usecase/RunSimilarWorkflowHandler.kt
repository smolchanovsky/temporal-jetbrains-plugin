package com.github.smolchanovsky.temporalplugin.usecase

import com.github.smolchanovsky.temporalplugin.cli.StartWorkflowCommand
import com.github.smolchanovsky.temporalplugin.cli.StartWorkflowResult
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateReader
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler

data class RunSimilarWorkflowRequest(
    val workflowId: String,
    val workflowType: String,
    val taskQueue: String,
    val input: String?
) : Request<Result<StartWorkflowResult>>

class RunSimilarWorkflowHandler(
    private val state: TemporalStateReader,
    private val mediatorProvider: () -> Mediator
) : RequestHandler<RunSimilarWorkflowRequest, Result<StartWorkflowResult>> {

    override suspend fun handle(request: RunSimilarWorkflowRequest): Result<StartWorkflowResult> {
        val connectionState = state.connectionState
        if (connectionState !is ConnectionState.Connected) {
            return Result.failure(IllegalStateException("Not connected"))
        }

        val result = mediatorProvider().send(
            StartWorkflowCommand(
                workflowId = request.workflowId,
                workflowType = request.workflowType,
                taskQueue = request.taskQueue,
                input = request.input,
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
