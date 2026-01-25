package com.github.smolchanovsky.temporalplugin.usecase

import com.github.smolchanovsky.temporalplugin.cli.GetWorkflowDetailsQuery
import com.github.smolchanovsky.temporalplugin.cli.GetWorkflowHistoryQuery
import com.github.smolchanovsky.temporalplugin.domain.Workflow
import com.github.smolchanovsky.temporalplugin.state.ConnectionState
import com.github.smolchanovsky.temporalplugin.state.TemporalStateReader
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

data class GenerateWorkflowDataRequest(
    val workflow: Workflow
) : Request<Result<GenerateWorkflowDataResult>>

data class GenerateWorkflowDataResult(
    val workflowId: String,
    val workflowType: String,
    val taskQueue: String,
    val input: String
)

class GenerateWorkflowDataHandler(
    private val state: TemporalStateReader,
    private val mediatorProvider: () -> Mediator
) : RequestHandler<GenerateWorkflowDataRequest, Result<GenerateWorkflowDataResult>> {

    private val json = Json { ignoreUnknownKeys = true }
    private val prettyJson = Json { prettyPrint = true }

    override suspend fun handle(request: GenerateWorkflowDataRequest): Result<GenerateWorkflowDataResult> {
        val connectionState = state.connectionState
        if (connectionState !is ConnectionState.Connected) {
            return Result.failure(IllegalStateException("Not connected"))
        }

        val workflow = request.workflow

        return try {
            coroutineScope {
                val detailsDeferred = async {
                    mediatorProvider().send(
                        GetWorkflowDetailsQuery(
                            workflowId = workflow.id,
                            runId = workflow.runId,
                            environment = connectionState.environment,
                            namespace = connectionState.namespace
                        )
                    )
                }

                val historyDeferred = async {
                    mediatorProvider().send(
                        GetWorkflowHistoryQuery(
                            workflowId = workflow.id,
                            runId = workflow.runId,
                            environment = connectionState.environment,
                            namespace = connectionState.namespace
                        )
                    )
                }

                val detailsResult = detailsDeferred.await()
                val historyResult = historyDeferred.await()

                if (detailsResult.isFailure) {
                    return@coroutineScope Result.failure(
                        detailsResult.exceptionOrNull() ?: Exception("Failed to load workflow details")
                    )
                }

                if (historyResult.isFailure) {
                    return@coroutineScope Result.failure(
                        historyResult.exceptionOrNull() ?: Exception("Failed to load workflow history")
                    )
                }

                val details = detailsResult.getOrThrow()
                val history = historyResult.getOrThrow()

                Result.success(
                    GenerateWorkflowDataResult(
                        workflowId = generateWorkflowId(details.workflowId),
                        workflowType = details.type,
                        taskQueue = details.taskQueue,
                        input = formatJson(history.input)
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateWorkflowId(originalId: String): String {
        val baseId = originalId.substringBeforeLast(WORKFLOW_ID_SEPARATOR)
        val suffix = System.currentTimeMillis().toString(36)
        return "$baseId$WORKFLOW_ID_SEPARATOR$suffix"
    }

    private fun formatJson(jsonString: String?): String {
        if (jsonString.isNullOrBlank()) return ""
        return try {
            val element = json.parseToJsonElement(jsonString)
            prettyJson.encodeToString(
                kotlinx.serialization.json.JsonElement.serializer(),
                element
            )
        } catch (e: Exception) {
            jsonString
        }
    }

    companion object {
        private const val WORKFLOW_ID_SEPARATOR = "--"
    }
}
