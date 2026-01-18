package com.github.smolchanovsky.temporalplugin.cli

import com.github.smolchanovsky.temporalplugin.cli.utils.CliExecutor
import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.domain.Namespace
import com.github.smolchanovsky.temporalplugin.domain.TimeUtils
import com.github.smolchanovsky.temporalplugin.domain.WorkflowDetails
import com.github.smolchanovsky.temporalplugin.domain.WorkflowStatus
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

data class GetWorkflowDetailsQuery(
    val workflowId: String,
    val runId: String,
    val environment: Environment,
    val namespace: Namespace
) : Request<Result<WorkflowDetails>>

@Serializable
private data class DescribeResponseDto(
    val workflowExecutionInfo: DescribeExecutionInfoDto
)

@Serializable
private data class DescribeExecutionInfoDto(
    val execution: DescribeExecutionDto,
    val type: DescribeWorkflowTypeDto,
    val startTime: String,
    val closeTime: String? = null,
    val status: String,
    val taskQueue: String,
    val historyLength: String? = null,
    val executionDuration: String? = null
)

@Serializable
private data class DescribeExecutionDto(
    val workflowId: String,
    val runId: String
)

@Serializable
private data class DescribeWorkflowTypeDto(
    val name: String
)

class GetWorkflowDetailsQueryHandler(
    private val cli: CliExecutor
) : RequestHandler<GetWorkflowDetailsQuery, Result<WorkflowDetails>> {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun handle(request: GetWorkflowDetailsQuery): Result<WorkflowDetails> {
        val args = buildList {
            addAll(listOf("workflow", "describe"))
            addAll(listOf("--workflow-id", request.workflowId))
            addAll(listOf("--run-id", request.runId))
            addAll(listOf("--namespace", request.namespace.name))
            addAll(listOf("--output", "json"))
            if (!request.environment.isLocal) {
                addAll(listOf("--env", request.environment.name))
            }
        }

        return cli.execute(*args.toTypedArray()).mapCatching { parseWorkflowDetails(it) }
    }

    private fun parseWorkflowDetails(jsonOutput: String): WorkflowDetails {
        val dto = json.decodeFromString<DescribeResponseDto>(jsonOutput)
        val info = dto.workflowExecutionInfo

        return WorkflowDetails(
            workflowId = info.execution.workflowId,
            runId = info.execution.runId,
            type = info.type.name,
            status = WorkflowStatus.fromString(info.status),
            taskQueue = info.taskQueue,
            startTime = TimeUtils.parseInstant(info.startTime) ?: Instant.EPOCH,
            closeTime = info.closeTime?.let { TimeUtils.parseInstant(it) },
            executionDuration = info.executionDuration?.let { TimeUtils.parseDuration(it) }
        )
    }
}
