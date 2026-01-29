package com.github.smolchanovsky.temporalplugin.cli

import com.github.smolchanovsky.temporalplugin.cli.utils.CliExecutor
import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.domain.Namespace
import com.github.smolchanovsky.temporalplugin.domain.TimeUtils
import com.github.smolchanovsky.temporalplugin.domain.Workflow
import com.github.smolchanovsky.temporalplugin.domain.WorkflowStatus
import com.intellij.openapi.diagnostic.thisLogger
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

data class GetWorkflowsQuery(
    val environment: Environment,
    val namespace: Namespace
) : Request<Result<List<Workflow>>>

@Serializable
private data class WorkflowExecutionDto(
    val workflowId: String,
    val runId: String
)

@Serializable
private data class WorkflowTypeDto(
    val name: String
)

@Serializable
private data class WorkflowListItemDto(
    val execution: WorkflowExecutionDto,
    val type: WorkflowTypeDto,
    val startTime: String,
    val closeTime: String? = null,
    val status: String,
    val taskQueue: String
) {
    fun toDomain(): Workflow {
        return Workflow(
            id = execution.workflowId,
            runId = execution.runId,
            type = type.name,
            taskQueue = taskQueue,
            status = WorkflowStatus.fromString(status),
            startTime = TimeUtils.parseInstant(startTime) ?: Instant.EPOCH,
            endTime = closeTime?.let { TimeUtils.parseInstant(it) }
        )
    }
}

class GetWorkflowsQueryHandler(
    private val cli: CliExecutor
) : RequestHandler<GetWorkflowsQuery, Result<List<Workflow>>> {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun handle(request: GetWorkflowsQuery): Result<List<Workflow>> {
        val args = buildList {
            addAll(listOf("workflow", "list"))
            addAll(listOf("--namespace", request.namespace.name))
            addAll(listOf("--output", "json"))
            if (!request.environment.isLocal) {
                addAll(listOf("--env", request.environment.name))
            }
        }

        return cli.execute(*args.toTypedArray()).map { parseWorkflowList(it) }
    }

    private fun parseWorkflowList(jsonOutput: String): List<Workflow> {
        if (jsonOutput.isBlank()) return emptyList()
        return try {
            json.decodeFromString<List<WorkflowListItemDto>>(jsonOutput).map { it.toDomain() }
        } catch (e: Exception) {
            thisLogger().error("Failed to parse workflow list JSON", e)
            emptyList()
        }
    }
}
