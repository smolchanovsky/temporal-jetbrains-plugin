package com.github.smolchanovsky.temporalplugin.domain

import java.time.Instant

data class WorkflowEvent(
    val eventId: Long,
    val eventType: WorkflowEventType,
    val timestamp: Instant,
    val attributes: List<EventAttribute> = emptyList(),
    val activityName: String? = null,
    val timerId: String? = null,
    val failureMessage: String? = null
)

data class EventAttribute(
    val label: String,
    val value: String
)
