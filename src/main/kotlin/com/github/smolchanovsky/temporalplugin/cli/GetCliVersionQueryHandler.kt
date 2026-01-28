package com.github.smolchanovsky.temporalplugin.cli

import com.github.smolchanovsky.temporalplugin.cli.utils.CliExecutor
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler

object GetCliVersionQuery : Request<Result<String>>

class GetCliVersionQueryHandler(
    private val cli: CliExecutor
) : RequestHandler<GetCliVersionQuery, Result<String>> {

    override suspend fun handle(request: GetCliVersionQuery): Result<String> {
        return cli.execute("--version").map { output ->
            // Output format: "temporal version X.Y.Z ..."
            output.trim()
                .removePrefix("temporal version ")
                .split(" ")
                .firstOrNull()
                ?: output.trim()
        }
    }
}
