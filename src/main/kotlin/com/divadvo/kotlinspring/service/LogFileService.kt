package com.divadvo.kotlinspring.service

import com.divadvo.kotlinspring.model.dto.LogEntry
import com.divadvo.kotlinspring.model.dto.LogResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class LogFileService(
    private val logParsingService: LogParsingService
) {
    
    private val logger = LoggerFactory.getLogger(LogFileService::class.java)
    private val logFile = File("logs/application.log")
    
    fun getLogs(lines: Int, since: Long?, level: String?): LogResponse {
        return try {
            if (!logFile.exists()) {
                logger.warn("Log file does not exist: ${logFile.absolutePath}")
                return LogResponse(emptyList(), 0, 0)
            }
            
            val allLines = Files.readAllLines(Paths.get(logFile.absolutePath))
            val entries = mutableListOf<LogEntry>()
            
            val filteredLines = if (since != null) {
                allLines.filter { line ->
                    val entry = logParsingService.parseLogLine(line)
                    entry != null && entry.timestamp.isAfter(
                        LocalDateTime.ofEpochSecond(since / 1000, ((since % 1000) * 1_000_000).toInt(),
                        ZoneOffset.UTC)
                    )
                }
            } else {
                allLines.takeLast(lines)
            }
            
            for (line in filteredLines) {
                val entry = logParsingService.parseLogLine(line)
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
    
    fun tailLogs(lines: Int, lastModified: Long?): LogResponse {
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
                val entry = logParsingService.parseLogLine(line)
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
}