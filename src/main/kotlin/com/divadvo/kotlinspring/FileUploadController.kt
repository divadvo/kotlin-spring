package com.divadvo.kotlinspring

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

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
        redirectAttributes: RedirectAttributes
    ): String {
        return if (!file.isEmpty) {
            try {
                val bookings = bookingService.processBookings(file, sourceType)
                val bookingsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bookings)
                
                redirectAttributes.addFlashAttribute("bookings", bookings)
                redirectAttributes.addFlashAttribute("bookingsJson", bookingsJson)
                redirectAttributes.addFlashAttribute("fileName", file.originalFilename)
                redirectAttributes.addFlashAttribute("selectedSourceType", sourceType)
                redirectAttributes.addFlashAttribute("success", true)
                
                "redirect:/upload"
            } catch (e: Exception) {
                redirectAttributes.addFlashAttribute("error", "Error processing file: ${e.message}")
                redirectAttributes.addFlashAttribute("selectedSourceType", sourceType)
                "redirect:/upload"
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload")
            "redirect:/upload"
        }
    }
}