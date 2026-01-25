package com.github.smolchanovsky.temporalplugin.ui.common

import com.github.smolchanovsky.temporalplugin.TextBundle
import com.github.smolchanovsky.temporalplugin.cli.utils.CliNotConfiguredException
import com.github.smolchanovsky.temporalplugin.cli.utils.ServerConnectionException
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun <T> Result<T>.onFailureNotify(project: Project): Result<T> = onFailure { error ->
    val message = when (error) {
        is CliNotConfiguredException -> TextBundle.message("error.cli.notConfigured")
        is ServerConnectionException -> TextBundle.message("error.server.connectionRefused")
        else -> error.message ?: TextBundle.message("error.unknown")
    }
    NotificationGroupManager.getInstance()
        .getNotificationGroup("TemporalErrors")
        .createNotification(message, NotificationType.ERROR)
        .notify(project)
}
