package com.divadvo.kotlinspring

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
class FileUploadController(
    private val bookingService: BookingService,
    private val objectMapper: ObjectMapper
) {

    @GetMapping("/upload")
    fun uploadForm(model: Model): String {
        model.addAttribute("sourceTypes", SourceType.values())
        return "upload"
    }

    @PostMapping("/upload")
    fun handleFileUpload(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("sourceType") sourceType: SourceType,
        model: Model
    ): String {
        return if (!file.isEmpty) {
            try {
                val bookings = bookingService.processBookings(file, sourceType)
                val bookingsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bookings)
                
                model.addAttribute("bookings", bookings)
                model.addAttribute("bookingsJson", bookingsJson)
                model.addAttribute("fileName", file.originalFilename)
                model.addAttribute("selectedSourceType", sourceType)
                model.addAttribute("sourceTypes", SourceType.values())
                "upload"
            } catch (e: Exception) {
                model.addAttribute("error", "Error processing file: ${e.message}")
                model.addAttribute("sourceTypes", SourceType.values())
                "upload"
            }
        } else {
            model.addAttribute("error", "Please select a file to upload")
            model.addAttribute("sourceTypes", SourceType.values())
            "upload"
        }
    }
}