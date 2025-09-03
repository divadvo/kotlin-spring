package com.divadvo.kotlinspring

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@RestController
@RequestMapping("/api/logs")
class LogController {
    
    private val logger = LoggerFactory.getLogger(LogController::class.java)
    private val logFile = File("logs/application.log")
    private val logPattern = Pattern.compile(
        """^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}) \[([^\]]+)\] (\w+)\s+([^\s]+) - (.*)$"""
    )
    
    @GetMapping
    fun getLogs(
        @RequestParam(defaultValue = "100") lines: Int,
        @RequestParam(required = false) since: Long?,
        @RequestParam(required = false) level: String?
    ): LogResponse {
        return try {
            if (!logFile.exists()) {
                logger.warn("Log file does not exist: ${logFile.absolutePath}")
                return LogResponse(emptyList(), 0, 0)
            }
            
            val allLines = Files.readAllLines(Paths.get(logFile.absolutePath))
            val entries = mutableListOf<LogEntry>()
            
            val filteredLines = if (since != null) {
                allLines.filter { line ->
                    val entry = parseLogLine(line)
                    entry != null && entry.timestamp.isAfter(
                        LocalDateTime.ofEpochSecond(since / 1000, ((since % 1000) * 1_000_000).toInt(), 
                        java.time.ZoneOffset.UTC)
                    )
                }
            } else {
                allLines.takeLast(lines)
            }
            
            for (line in filteredLines) {
                val entry = parseLogLine(line)
                if (entry != null) {
                    if (level == null || entry.level.equals(level, ignoreCase = true)) {
                        entries.add(entry)
                    }
                }
            }
            
            LogResponse(
                entries = entries.takeLast(lines),
                totalLines = allLines.size.toLong(),
                lastModified = logFile.lastModified()
            )
        } catch (e: Exception) {
            logger.error("Error reading log file", e)
            LogResponse(emptyList(), 0, 0)
        }
    }
    
    @GetMapping("/tail")
    fun tailLogs(
        @RequestParam(defaultValue = "50") lines: Int,
        @RequestParam(required = false) lastModified: Long?
    ): LogResponse {
        return try {
            if (!logFile.exists()) {
                return LogResponse(emptyList(), 0, 0)
            }
            
            val currentLastModified = logFile.lastModified()
            
            // If file hasn't changed, return empty response
            if (lastModified != null && currentLastModified <= lastModified) {
                return LogResponse(emptyList(), 0, currentLastModified)
            }
            
            val allLines = Files.readAllLines(Paths.get(logFile.absolutePath))
            val entries = mutableListOf<LogEntry>()
            
            for (line in allLines.takeLast(lines)) {
                val entry = parseLogLine(line)
                if (entry != null) {
                    entries.add(entry)
                }
            }
            
            LogResponse(
                entries = entries,
                totalLines = allLines.size.toLong(),
                lastModified = currentLastModified
            )
        } catch (e: Exception) {
            logger.error("Error tailing log file", e)
            LogResponse(emptyList(), 0, System.currentTimeMillis())
        }
    }
    
    private fun parseLogLine(line: String): LogEntry? {
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