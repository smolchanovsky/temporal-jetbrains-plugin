package com.github.smolchanovsky.temporalplugin.ui.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

object FormatUtils {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    fun formatTime(instant: Instant): String {
        return timeFormatter.format(instant)
    }

    fun formatDateTime(instant: Instant): String {
        return dateTimeFormatter.format(instant)
    }

    fun formatDuration(duration: Duration): String {
        val totalSeconds = duration.inWholeSeconds
        val millis = duration.inWholeMilliseconds

        return when {
            millis < 1000 -> "${millis}ms"
            totalSeconds < 60 -> {
                val ms = millis % 1000
                if (ms > 0) "${totalSeconds}.${String.format("%03d", ms).trimEnd('0')}s"
                else "${totalSeconds}s"
            }
            totalSeconds < 3600 -> {
                val mins = totalSeconds / 60
                val secs = totalSeconds % 60
                if (secs > 0) "${mins}m ${secs}s" else "${mins}m"
            }
            else -> {
                val hours = totalSeconds / 3600
                val mins = (totalSeconds % 3600) / 60
                if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
            }
        }
    }
}