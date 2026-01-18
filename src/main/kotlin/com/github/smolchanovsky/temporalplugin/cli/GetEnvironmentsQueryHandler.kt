package com.github.smolchanovsky.temporalplugin.cli

import com.github.smolchanovsky.temporalplugin.cli.utils.CliExecutor
import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.intellij.openapi.diagnostic.thisLogger
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object GetEnvironmentsQuery : Request<Result<List<Environment>>>

@Serializable
private data class EnvironmentDto(
    @SerialName("name") val name: String
) {
    fun toDomain(): Environment = Environment(name)
}

class GetEnvironmentsQueryHandler(
    private val cli: CliExecutor
) : RequestHandler<GetEnvironmentsQuery, Result<List<Environment>>> {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun handle(request: GetEnvironmentsQuery): Result<List<Environment>> {
        return cli.execute("env", "list", "--output", "json").map { parseEnvironmentList(it) }
    }

    private fun parseEnvironmentList(jsonOutput: String): List<Environment> {
        if (jsonOutput.isBlank()) return emptyList()
        return try {
            json.decodeFromString<List<EnvironmentDto>>(jsonOutput).map { it.toDomain() }
        } catch (e: Exception) {
            thisLogger().error("Failed to parse environment list JSON", e)
            emptyList()
        }
    }
}
