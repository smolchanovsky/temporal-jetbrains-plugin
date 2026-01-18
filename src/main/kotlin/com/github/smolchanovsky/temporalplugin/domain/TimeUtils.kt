package com.github.smolchanovsky.temporalplugin.domain

import java.time.Instant
import java.time.format.DateTimeParseException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object TimeUtils {

    fun parseInstant(isoTime: String): Instant? {
        if (isoTime.isBlank()) return null
        return try {
            Instant.parse(isoTime)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    fun parseDuration(duration: String): Duration? {
        if (duration.isBlank()) return null
        return try {
            val seconds = duration.removeSuffix("s").toDoubleOrNull() ?: return null
            seconds.seconds
        } catch (e: Exception) {
            null
        }
    }
}
