package com.github.smolchanovsky.temporalplugin.usecase

import com.github.smolchanovsky.temporalplugin.domain.Workflow
import com.github.smolchanovsky.temporalplugin.domain.WorkflowDetails
import com.github.smolchanovsky.temporalplugin.domain.WorkflowHistory
import com.github.smolchanovsky.temporalplugin.cli.GetWorkflowDetailsQuery
import com.github.smolchanovsky.temporalplugin.cli.GetWorkflowHistoryQuery
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateWriter
import com.github.smolchanovsky.temporalplugin.state.ViewState
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class LoadWorkflowDetailsUseCase(
    val workflow: Workflow
) : Request<Result<Pair<WorkflowDetails, WorkflowHistory>>>

class LoadWorkflowDetailsUseCaseHandler(
    private val state: TemporalStateWriter,
    private val mediatorProvider: () -> Mediator
) : RequestHandler<LoadWorkflowDetailsUseCase, Result<Pair<WorkflowDetails, WorkflowHistory>>> {

    override suspend fun handle(request: LoadWorkflowDetailsUseCase): Result<Pair<WorkflowDetails, WorkflowHistory>> {
        val connectionState = state.connectionState
        if (connectionState !is ConnectionState.Connected) {
            return Result.failure(Exception("Not connected"))
        }

        val workflow = request.workflow
        val environment = connectionState.environment
        val namespace = connectionState.namespace

        return try {
            coroutineScope {
                // Load details and history in parallel
                val detailsDeferred = async {
                    mediatorProvider().send(
                        GetWorkflowDetailsQuery(
                            workflowId = workflow.id,
                            runId = workflow.runId,
                            environment = environment,
                            namespace = namespace
                        )
                    )
                }

                val historyDeferred = async {
                    mediatorProvider().send(
                        GetWorkflowHistoryQuery(
                            workflowId = workflow.id,
                            runId = workflow.runId,
                            environment = environment,
                            namespace = namespace
                        )
                    )
                }

                val detailsResult = detailsDeferred.await()
                val historyResult = historyDeferred.await()

                if (detailsResult.isFailure) {
                    val error = detailsResult.exceptionOrNull()?.message ?: "Failed to load workflow details"
                    state.updateViewState(
                        ViewState.WorkflowDetailsView(
                            workflow = workflow,
                            isLoading = false,
                            error = error
                        )
                    )
                    return@coroutineScope Result.failure(detailsResult.exceptionOrNull() ?: Exception(error))
                }

                if (historyResult.isFailure) {
                    val error = historyResult.exceptionOrNull()?.message ?: "Failed to load workflow history"
                    state.updateViewState(
                        ViewState.WorkflowDetailsView(
                            workflow = workflow,
                            isLoading = false,
                            error = error
                        )
                    )
                    return@coroutineScope Result.failure(historyResult.exceptionOrNull() ?: Exception(error))
                }

                val details = detailsResult.getOrThrow()
                val history = historyResult.getOrThrow()

                state.updateViewState(
                    ViewState.WorkflowDetailsView(
                        workflow = workflow,
                        details = details,
                        history = history,
                        isLoading = false
                    )
                )

                Result.success(Pair(details, history))
            }
        } catch (e: Exception) {
            state.updateViewState(
                ViewState.WorkflowDetailsView(
                    workflow = workflow,
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            )
            Result.failure(e)
        }
    }
}
