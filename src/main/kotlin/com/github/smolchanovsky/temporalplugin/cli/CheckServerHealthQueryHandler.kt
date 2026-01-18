package com.github.smolchanovsky.temporalplugin.cli

import com.github.smolchanovsky.temporalplugin.cli.utils.CliExecutor
import com.intellij.openapi.diagnostic.thisLogger
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class CheckServerHealthQuery(
    val serverAddress: String
) : Request<Result<Boolean>>

@Serializable
private data class HealthStatusDto(
    @SerialName("status") val status: String
)

class CheckServerHealthQueryHandler(
    private val cli: CliExecutor
) : RequestHandler<CheckServerHealthQuery, Result<Boolean>> {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun handle(request: CheckServerHealthQuery): Result<Boolean> {
        return cli.execute(
            "operator", "cluster", "health",
            "--address", request.serverAddress,
            "--output", "json"
        ).map { parseHealthStatus(it) }.recoverCatching { false }
    }

    private fun parseHealthStatus(jsonOutput: String): Boolean {
        if (jsonOutput.isBlank()) return false
        return try {
            json.decodeFromString<HealthStatusDto>(jsonOutput).status == "SERVING"
        } catch (e: Exception) {
            thisLogger().warn("Failed to parse health status JSON")
            jsonOutput.contains("SERVING", ignoreCase = true)
        }
    }
}
