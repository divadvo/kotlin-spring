package com.divadvo.kotlinspring

import java.time.LocalDateTime

data class LogEntry(
    val timestamp: LocalDateTime,
    val level: String,
    val logger: String,
    val message: String,
    val thread: String? = null
)

data class LogResponse(
    val entries: List<LogEntry>,
    val totalLines: Long,
    val lastModified: Long
)