package com.github.smolchanovsky.temporalplugin.cli

import com.github.smolchanovsky.temporalplugin.cli.utils.CliExecutor
import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.domain.Namespace
import com.intellij.openapi.diagnostic.thisLogger
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class GetNamespacesQuery(
    val environment: Environment
) : Request<Result<List<Namespace>>>

@Serializable
private data class NamespaceInfoDto(
    @SerialName("name") val name: String
)

@Serializable
private data class NamespaceDto(
    @SerialName("namespaceInfo") val namespaceInfo: NamespaceInfoDto
) {
    fun toDomain(): Namespace = Namespace(namespaceInfo.name)
}

class GetNamespacesQueryHandler(
    private val cli: CliExecutor
) : RequestHandler<GetNamespacesQuery, Result<List<Namespace>>> {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun handle(request: GetNamespacesQuery): Result<List<Namespace>> {
        val args = buildList {
            addAll(listOf("operator", "namespace", "list"))
            addAll(listOf("--output", "json"))
            if (!request.environment.isLocal) {
                addAll(listOf("--env", request.environment.name))
            }
        }

        return cli.execute(*args.toTypedArray()).map { parseNamespaceList(it) }
    }

    private fun parseNamespaceList(jsonOutput: String): List<Namespace> {
        if (jsonOutput.isBlank()) return emptyList()
        return try {
            json.decodeFromString<List<NamespaceDto>>(jsonOutput).map { it.toDomain() }
        } catch (e: Exception) {
            thisLogger().error("Failed to parse namespace list JSON", e)
            emptyList()
        }
    }
}
