package com.github.smolchanovsky.temporalplugin.domain

enum class WorkflowStatus(val displayName: String, val cliValue: String) {
    RUNNING("Running", "Running"),
    COMPLETED("Completed", "Completed"),
    FAILED("Failed", "Failed"),
    CANCELED("Canceled", "Canceled"),
    TERMINATED("Terminated", "Terminated"),
    CONTINUED_AS_NEW("Continued As New", "ContinuedAsNew"),
    TIMED_OUT("Timed Out", "TimedOut"),
    UNKNOWN("Unknown", "Unknown");

    companion object {
        fun fromString(value: String): WorkflowStatus {
            val normalized = value
                .removePrefix("WORKFLOW_EXECUTION_STATUS_")
                .uppercase()
            return entries.find { it.name == normalized } ?: UNKNOWN
        }
    }
}
