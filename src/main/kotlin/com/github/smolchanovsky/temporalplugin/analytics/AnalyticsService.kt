package com.github.smolchanovsky.temporalplugin.analytics

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*

@Service(Service.Level.APP)
class AnalyticsService(private val scope: CoroutineScope) {

    private val settings = AnalyticsSettings.getInstance()
    private val state = AnalyticsState.getInstance()

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    fun track(event: AnalyticsEvent) {
        if (!settings.analyticsEnabled) {
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                sendEvent(event)
            } catch (e: Exception) {
                thisLogger().debug("Failed to send analytics event: ${event.name}", e)
            }
        }
    }

    private fun sendEvent(event: AnalyticsEvent) {
        val token = AnalyticsConfig.projectToken
        if (token.isEmpty()) {
            thisLogger().debug("Mixpanel token not configured, skipping event: ${event.name}")
            return
        }

        val payload = buildJsonArray {
            add(buildJsonObject {
                put("event", event.name)
                put("properties", buildJsonObject {
                    put("token", token)
                    put("distinct_id", settings.installationId)
                    put("time", System.currentTimeMillis() / 1000)

                    // Add user properties
                    state.userProperties.forEach { (key, value) ->
                        when (value) {
                            is Boolean -> put(key, value)
                            is Number -> put(key, value)
                            else -> put(key, value.toString())
                        }
                    }

                    // Add temporal CLI version if available
                    state.temporalCliVersion?.let { put("temporal_cli_version", it) }

                    // Add event-specific properties
                    event.properties.forEach { (key, value) ->
                        when (value) {
                            is Boolean -> put(key, value)
                            is Number -> put(key, value)
                            else -> put(key, value.toString())
                        }
                    }
                })
            })
        }

        val payloadString = payload.toString()
        val encodedData = Base64.getEncoder().encodeToString(payloadString.toByteArray())

        thisLogger().debug("Sending analytics event: ${event.name}")

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$MIXPANEL_ENDPOINT?data=$encodedData"))
            .header("Accept", "text/plain")
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 200 && response.body() == "1") {
            thisLogger().debug("Analytics event sent successfully: ${event.name}")
        } else {
            thisLogger().warn("Analytics request failed: ${response.body()}")
        }
    }

    companion object {
        private const val MIXPANEL_ENDPOINT = "https://api.mixpanel.com/track"

        fun getInstance(): AnalyticsService =
            ApplicationManager.getApplication().getService(AnalyticsService::class.java)
    }
}
