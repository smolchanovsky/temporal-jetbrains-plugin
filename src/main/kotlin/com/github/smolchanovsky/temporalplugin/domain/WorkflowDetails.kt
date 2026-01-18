package com.github.smolchanovsky.temporalplugin.domain

import java.time.Instant
import kotlin.time.Duration

data class WorkflowDetails(
    val workflowId: String,
    val runId: String,
    val type: String,
    val status: WorkflowStatus,
    val taskQueue: String,
    val startTime: Instant,
    val closeTime: Instant?,
    val executionDuration: Duration?
)
