package com.github.smolchanovsky.temporalplugin.cli

import com.github.smolchanovsky.temporalplugin.cli.utils.CliExecutor
import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.domain.Namespace
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler

data class CancelWorkflowCommand(
    val workflowId: String,
    val runId: String,
    val environment: Environment,
    val namespace: Namespace
) : Request<Result<Unit>>

class CancelWorkflowCommandHandler(
    private val cli: CliExecutor
) : RequestHandler<CancelWorkflowCommand, Result<Unit>> {

    override suspend fun handle(request: CancelWorkflowCommand): Result<Unit> {
        val args = buildList {
            addAll(listOf("workflow", "cancel"))
            addAll(listOf("--workflow-id", request.workflowId))
            addAll(listOf("--run-id", request.runId))
            addAll(listOf("--namespace", request.namespace.name))
            if (!request.environment.isLocal) {
                addAll(listOf("--env", request.environment.name))
            }
        }

        return cli.execute(*args.toTypedArray()).map { }
    }
}
