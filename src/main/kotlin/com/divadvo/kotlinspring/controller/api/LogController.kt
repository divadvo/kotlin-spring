package com.divadvo.kotlinspring.controller.api

import com.divadvo.kotlinspring.model.dto.LogResponse
import com.divadvo.kotlinspring.service.LogFileService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/logs")
class LogController(
    private val logFileService: LogFileService
) {

    @GetMapping
    fun getLogs(
        @RequestParam(defaultValue = "100") lines: Int,
        @RequestParam(required = false) since: Long?,
        @RequestParam(required = false) level: String?
    ): LogResponse {
        return logFileService.getLogs(lines, since, level)
    }

    @GetMapping("/tail")
    fun tailLogs(
        @RequestParam(defaultValue = "50") lines: Int,
        @RequestParam(required = false) lastModified: Long?
    ): LogResponse {
        return logFileService.tailLogs(lines, lastModified)
    }

}