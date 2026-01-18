package com.github.smolchanovsky.temporalplugin.domain

enum class WorkflowStatus(val displayName: String) {
    RUNNING("Running"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELED("Canceled"),
    TERMINATED("Terminated"),
    CONTINUED_AS_NEW("Continued As New"),
    TIMED_OUT("Timed Out"),
    UNKNOWN("Unknown");

    companion object {
        fun fromString(value: String): WorkflowStatus {
            val normalized = value
                .removePrefix("WORKFLOW_EXECUTION_STATUS_")
                .uppercase()
            return entries.find { it.name == normalized } ?: UNKNOWN
        }
    }
}
