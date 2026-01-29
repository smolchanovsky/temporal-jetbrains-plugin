package com.github.smolchanovsky.temporalplugin.domain

import java.time.Instant

data class Workflow(
    val id: String,
    val runId: String,
    val type: String,
    val taskQueue: String,
    val status: WorkflowStatus,
    val startTime: Instant,
    val endTime: Instant?
)
