package com.github.smolchanovsky.temporalplugin.cli

import com.github.smolchanovsky.temporalplugin.cli.utils.CliExecutor
import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.domain.Namespace
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler

data class TerminateWorkflowCommand(
    val workflowId: String,
    val runId: String,
    val reason: String?,
    val environment: Environment,
    val namespace: Namespace
) : Request<Result<Unit>>

class TerminateWorkflowCommandHandler(
    private val cli: CliExecutor
) : RequestHandler<TerminateWorkflowCommand, Result<Unit>> {

    override suspend fun handle(request: TerminateWorkflowCommand): Result<Unit> {
        val args = buildList {
            addAll(listOf("workflow", "terminate"))
            addAll(listOf("--workflow-id", request.workflowId))
            addAll(listOf("--run-id", request.runId))
            addAll(listOf("--namespace", request.namespace.name))
            if (!request.reason.isNullOrBlank()) {
                addAll(listOf("--reason", request.reason))
            }
            if (!request.environment.isLocal) {
                addAll(listOf("--env", request.environment.name))
            }
        }

        return cli.execute(*args.toTypedArray()).map { }
    }
}
