package com.github.smolchanovsky.temporalplugin.ui.common

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.analytics.AnalyticsService
import com.github.smolchanovsky.temporalplugin.analytics.ErrorEvent
import com.github.smolchanovsky.temporalplugin.cli.utils.CliNotConfiguredException
import com.github.smolchanovsky.temporalplugin.cli.utils.ServerConnectionException
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

private val analytics get() = AnalyticsService.getInstance()

fun <T> Result<T>.onFailureNotify(project: Project): Result<T> = onFailure { error ->
    val (errorType, message) = when (error) {
        is CliNotConfiguredException -> "cli_not_configured" to TextBundle.message("error.cli.notConfigured")
        is ServerConnectionException -> "server_connection_refused" to TextBundle.message("error.server.connectionRefused")
        else -> "unknown" to (error.message ?: TextBundle.message("error.unknown"))
    }

    analytics.track(ErrorEvent(errorType, error::class.simpleName))

    NotificationGroupManager.getInstance()
        .getNotificationGroup("TemporalErrors")
        .createNotification(message, NotificationType.ERROR)
        .notify(project)
}
