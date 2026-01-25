package com.github.smolchanovsky.temporalplugin.cli

import com.github.smolchanovsky.temporalplugin.cli.utils.CliExecutor
import com.github.smolchanovsky.temporalplugin.domain.Environment
import com.github.smolchanovsky.temporalplugin.domain.EventAttribute
import com.github.smolchanovsky.temporalplugin.domain.Namespace
import com.github.smolchanovsky.temporalplugin.domain.TimeUtils
import com.github.smolchanovsky.temporalplugin.domain.WorkflowEvent
import com.github.smolchanovsky.temporalplugin.domain.WorkflowEventType
import com.github.smolchanovsky.temporalplugin.domain.WorkflowHistory
import com.intellij.openapi.diagnostic.thisLogger
import com.trendyol.kediatr.Request
import com.trendyol.kediatr.RequestHandler
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.util.Base64

data class GetWorkflowHistoryQuery(
    val workflowId: String,
    val runId: String,
    val environment: Environment,
    val namespace: Namespace
) : Request<Result<WorkflowHistory>>

class GetWorkflowHistoryQueryHandler(
    private val cli: CliExecutor
) : RequestHandler<GetWorkflowHistoryQuery, Result<WorkflowHistory>> {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun handle(request: GetWorkflowHistoryQuery): Result<WorkflowHistory> {
        val args = buildList {
            addAll(listOf("workflow", "show"))
            addAll(listOf("--workflow-id", request.workflowId))
            addAll(listOf("--run-id", request.runId))
            addAll(listOf("--namespace", request.namespace.name))
            addAll(listOf("--output", "json"))
            if (!request.environment.isLocal) {
                addAll(listOf("--env", request.environment.name))
            }
        }

        return cli.execute(*args.toTypedArray()).map { parseWorkflowHistory(it) }
    }

    private fun parseWorkflowHistory(jsonOutput: String): WorkflowHistory {
        if (jsonOutput.isBlank()) return WorkflowHistory(emptyList())

        return try {
            val rootJson = json.parseToJsonElement(jsonOutput).jsonObject
            val eventsArray = rootJson["events"]?.jsonArray ?: return WorkflowHistory(emptyList())
            val events = eventsArray.mapNotNull { parseEvent(it.jsonObject) }
            WorkflowHistory(events)
        } catch (e: Exception) {
            thisLogger().error("Failed to parse workflow history JSON", e)
            WorkflowHistory(emptyList())
        }
    }

    private fun parseEvent(eventJson: JsonObject): WorkflowEvent? {
        val eventType = eventJson["eventType"]?.jsonPrimitive?.contentOrNull ?: return null
        val eventId = eventJson["eventId"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L
        val eventTime = eventJson["eventTime"]?.jsonPrimitive?.contentOrNull ?: ""
        val attrsJson = findAttributesObject(eventJson, eventType)

        return WorkflowEvent(
            eventId = eventId,
            eventType = WorkflowEventType.fromString(eventType),
            timestamp = TimeUtils.parseInstant(eventTime) ?: Instant.EPOCH,
            attributes = parseAttributes(attrsJson),
            activityName = attrsJson?.get("activityType")?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull,
            timerId = attrsJson?.get("timerId")?.jsonPrimitive?.contentOrNull,
            failureMessage = attrsJson?.get("failure")?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
        )
    }

    private fun findAttributesObject(eventJson: JsonObject, eventType: String): JsonObject? {
        val fieldName = eventType
            .removePrefix("EVENT_TYPE_")
            .lowercase()
            .replace(Regex("_([a-z])")) { it.groupValues[1].uppercase() } + "EventAttributes"
        return eventJson[fieldName]?.jsonObject
    }

    private fun parseAttributes(attrsJson: JsonObject?): List<EventAttribute> {
        if (attrsJson == null) return emptyList()
        return attrsJson.entries.mapNotNull { (key, value) ->
            parseValue(key, value)?.let { EventAttribute(formatLabel(key), it) }
        }
    }

    private fun parseValue(key: String, value: JsonElement): String? {
        return when (value) {
            is JsonPrimitive -> {
                val content = value.contentOrNull
                if (content.isNullOrBlank() || content == "0s") null else content
            }
            is JsonObject -> {
                value["name"]?.jsonPrimitive?.contentOrNull
                    ?: value["payloads"]?.jsonArray?.let { decodePayload(it) }
                    ?: value.toString().takeIf { value.isNotEmpty() }
            }
            is JsonArray -> {
                if (key in listOf("input", "result", "payloads")) {
                    decodePayload(value)
                } else if (value.isNotEmpty()) {
                    "${value.size} item(s)"
                } else null
            }
        }
    }

    private fun decodePayload(array: JsonArray): String? {
        for (item in array) {
            val data = (item as? JsonObject)?.get("data")?.jsonPrimitive?.contentOrNull
            if (data != null) {
                return try {
                    String(Base64.getDecoder().decode(data), Charsets.UTF_8)
                } catch (e: Exception) {
                    null
                }
            }
        }
        return null
    }

    private fun formatLabel(key: String): String {
        return key
            .replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]} ${it.groupValues[2]}" }
            .replaceFirstChar { it.uppercase() }
    }
}
