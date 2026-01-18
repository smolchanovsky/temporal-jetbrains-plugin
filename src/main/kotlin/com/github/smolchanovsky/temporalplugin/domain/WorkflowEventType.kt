package com.github.smolchanovsky.temporalplugin.domain

enum class EventCategory {
    EXECUTION,
    TASK,
    ACTIVITY,
    TIMER,
    CHILD_WORKFLOW,
    SIGNAL,
    MARKER,
    OTHER
}

@JvmInline
value class WorkflowEventType(val name: String) {

    val displayName: String
        get() = name
            .removePrefix("WORKFLOW_")
            .removePrefix("ACTIVITY_")
            .removePrefix("TIMER_")
            .replace("_", " ")
            .lowercase()
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

    val category: EventCategory
        get() = when {
            name.startsWith("WORKFLOW_EXECUTION_") -> EventCategory.EXECUTION
            name.startsWith("WORKFLOW_TASK_") -> EventCategory.TASK
            name.startsWith("ACTIVITY_") -> EventCategory.ACTIVITY
            name.startsWith("TIMER_") -> EventCategory.TIMER
            name.contains("CHILD_WORKFLOW") -> EventCategory.CHILD_WORKFLOW
            name.contains("SIGNAL") -> EventCategory.SIGNAL
            name == "MARKER_RECORDED" -> EventCategory.MARKER
            else -> EventCategory.OTHER
        }

    val isStarted: Boolean
        get() = name.endsWith("_STARTED")

    val isScheduled: Boolean
        get() = name.endsWith("_SCHEDULED")

    val isInitiated: Boolean
        get() = name.endsWith("_INITIATED")

    val isCompleted: Boolean
        get() = name.endsWith("_COMPLETED")

    val isFired: Boolean
        get() = name.endsWith("_FIRED")

    val isFailed: Boolean
        get() = name.endsWith("_FAILED")

    val isCanceled: Boolean
        get() = name.endsWith("_CANCELED")

    val isTerminated: Boolean
        get() = name.endsWith("_TERMINATED")

    val isTimedOut: Boolean
        get() = name.endsWith("_TIMED_OUT")

    val isSignaled: Boolean
        get() = name.endsWith("_SIGNALED")

    companion object {
        fun fromString(value: String): WorkflowEventType {
            val normalized = value
                .removePrefix("EVENT_TYPE_")
                .uppercase()
            return WorkflowEventType(normalized)
        }
    }
}
