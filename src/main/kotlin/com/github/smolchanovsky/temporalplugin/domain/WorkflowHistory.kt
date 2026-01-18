package com.github.smolchanovsky.temporalplugin.domain

data class WorkflowHistory(val events: List<WorkflowEvent>) {

    val input: String?
        get() {
            val startedEvent = events.find {
                it.eventType.category == EventCategory.EXECUTION && it.eventType.isStarted
            }
            return startedEvent?.attributes?.find {
                it.label.startsWith("Input")
            }?.value
        }

    val result: String?
        get() {
            val completedEvent = events.find {
                it.eventType.category == EventCategory.EXECUTION && it.eventType.isCompleted
            }
            return completedEvent?.attributes?.find {
                it.label.startsWith("Result")
            }?.value
        }
}
