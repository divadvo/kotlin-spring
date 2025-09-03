package com.divadvo.kotlinspring.controller.web

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AdminController {

    private val logger = LoggerFactory.getLogger(AdminController::class.java)

    @GetMapping(value = ["/my-uploader/logs", "/my-uploader/logs/"])
    fun logs(model: Model): String {
        logger.info("Accessing Logs page")
        return "logs"
    }

    @GetMapping(value = ["/my-uploader/db", "/my-uploader/db/"])
    fun db(model: Model): String {
        logger.info("Accessing DB Viewer page")
        return "db"
    }
}