package com.divadvo.kotlinspring.service

import com.divadvo.kotlinspring.model.dto.LogEntry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@Service
class LogParsingService {
    
    private val logger = LoggerFactory.getLogger(LogParsingService::class.java)
    private val logPattern = Pattern.compile(
        """^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}) \[([^\]]+)\] (\w+)\s+([^\s]+) - (.*)$"""
    )
    
    fun parseLogLine(line: String): LogEntry? {
        val matcher = logPattern.matcher(line)
        return if (matcher.matches()) {
            try {
                val timestamp = LocalDateTime.parse(
                    matcher.group(1),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                )
                LogEntry(
                    timestamp = timestamp,
                    thread = matcher.group(2),
                    level = matcher.group(3),
                    logger = matcher.group(4),
                    message = matcher.group(5)
                )
            } catch (e: Exception) {
                logger.debug("Failed to parse log line: $line", e)
                null
            }
        } else {
            // Handle multi-line entries (like stack traces)
            if (line.trim().isNotEmpty() && !line.startsWith(" ")) {
                LogEntry(
                    timestamp = LocalDateTime.now(),
                    level = "INFO",
                    logger = "unknown",
                    message = line,
                    thread = null
                )
            } else {
                null
            }
        }
    }
}