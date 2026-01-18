package com.github.smolchanovsky.temporalplugin.cli.utils

import com.github.smolchanovsky.temporalplugin.cli.utils.CliNotConfiguredException
import com.github.smolchanovsky.temporalplugin.cli.TemporalCliConfig
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.awaitExit
import java.io.BufferedReader
import java.io.InputStreamReader

class CliExecutor(private val config: TemporalCliConfig) {

    suspend fun execute(vararg args: String): Result<String> {
        val cliPath = config.temporalCliPath
            ?: return Result.failure(CliNotConfiguredException())

        val command = listOf(cliPath) + args
        LOG.info("Executing command: ${command.joinToString(" ")}")

        return try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(false)
                .start()

            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val errorOutput = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            val exitCode = process.awaitExit()

            if (exitCode == 0) {
                Result.success(output)
            } else {
                Result.failure(buildError(exitCode, output, errorOutput))
            }
        } catch (e: Exception) {
            LOG.error("Error executing CLI command", e)
            if (e.message?.contains("Cannot run program") == true) {
                Result.failure(CliNotConfiguredException())
            } else {
                Result.failure(e)
            }
        }
    }

    private fun buildError(exitCode: Int, output: String, errorOutput: String): Exception {
        val message = when {
            errorOutput.isNotBlank() -> errorOutput.trim()
            output.contains("error", ignoreCase = true) -> output.trim()
            else -> "Command failed with exit code $exitCode"
        }

        return if (message.contains("executable file not found") || message.contains("command not found")) {
            CliNotConfiguredException()
        } else {
            Exception(message)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(CliExecutor::class.java)
    }
}