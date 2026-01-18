package com.github.smolchanovsky.temporalplugin.ui.settings

import java.io.File

object TemporalCliDetector {

    fun detect(): String? {
        return detectViaCommand() ?: detectInCommonPaths()
    }

    private fun detectViaCommand(): String? {
        val commands = if (System.getProperty("os.name").lowercase().contains("win")) {
            listOf(listOf("where", "temporal"))
        } else {
            listOf(
                listOf("which", "temporal"),
                listOf("/bin/sh", "-c", "command -v temporal")
            )
        }

        for (command in commands) {
            try {
                val process = ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().readText().trim()
                val exitCode = process.waitFor()
                if (exitCode == 0 && output.isNotBlank()) {
                    val path = output.lines().first().trim()
                    if (File(path).exists()) {
                        return path
                    }
                }
            } catch (_: Exception) {
                // Continue to next command
            }
        }
        return null
    }

    private fun detectInCommonPaths(): String? {
        val commonPaths = listOf(
            "/usr/local/bin/temporal",
            "/opt/homebrew/bin/temporal",
            "/usr/bin/temporal",
            System.getProperty("user.home") + "/.local/bin/temporal"
        )
        return commonPaths.firstOrNull { File(it).exists() }
    }
}
