package com.github.smolchanovsky.temporalplugin.cli

import com.github.smolchanovsky.temporalplugin.cli.utils.CliExecutor
import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.domain.Namespace
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class StartWorkflowCommand(
    val workflowId: String,
    val workflowType: String,
    val taskQueue: String,
    val input: String?,
    val environment: Environment,
    val namespace: Namespace
) : Request<Result<StartWorkflowResult>>

data class StartWorkflowResult(
    val workflowId: String,
    val runId: String
)

@Serializable
private data class StartWorkflowResponseDto(
    val workflowId: String,
    val runId: String
)

class StartWorkflowCommandHandler(
    private val cli: CliExecutor
) : RequestHandler<StartWorkflowCommand, Result<StartWorkflowResult>> {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun handle(request: StartWorkflowCommand): Result<StartWorkflowResult> {
        val args = buildList {
            addAll(listOf("workflow", "start"))
            addAll(listOf("--workflow-id", request.workflowId))
            addAll(listOf("--type", request.workflowType))
            addAll(listOf("--task-queue", request.taskQueue))
            addAll(listOf("--namespace", request.namespace.name))
            addAll(listOf("--output", "json"))
            if (!request.input.isNullOrBlank()) {
                addAll(listOf("--input", request.input))
            }
            if (!request.environment.isLocal) {
                addAll(listOf("--env", request.environment.name))
            }
        }

        return cli.execute(*args.toTypedArray()).mapCatching { parseResponse(it) }
    }

    private fun parseResponse(jsonOutput: String): StartWorkflowResult {
        val dto = json.decodeFromString<StartWorkflowResponseDto>(jsonOutput)
        return StartWorkflowResult(
            workflowId = dto.workflowId,
            runId = dto.runId
        )
    }
}
