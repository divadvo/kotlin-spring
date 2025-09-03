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
    fun handleDataProcessing(
        @RequestParam("inputMode") inputMode: String,
        @RequestParam("sourceType") sourceType: SourceType,
        @RequestParam(value = "file", required = false) file: MultipartFile?,
        @RequestParam(value = "textContent", required = false) textContent: String?,
        @RequestParam(value = "predefinedFile", required = false) predefinedFile: String?,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            val bookings = when (inputMode) {
                "file" -> {
                    if (file == null || file.isEmpty) {
                        throw IllegalArgumentException("Please select a file to upload")
                    }
                    bookingService.processBookings(file, sourceType)
                }
                "text" -> {
                    if (textContent.isNullOrBlank()) {
                        throw IllegalArgumentException("Please enter CSV content")
                    }
                    bookingService.processBookingsFromText(textContent, sourceType)
                }
                "predefined" -> {
                    if (predefinedFile.isNullOrBlank()) {
                        throw IllegalArgumentException("Please select a sample data file")
                    }
                    bookingService.processBookingsFromPredefinedFile(predefinedFile, sourceType)
                }
                else -> throw IllegalArgumentException("Invalid input mode selected")
            }
            
            if (bookings.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No valid booking data found. Please ensure the data contains comma-separated values (customerName,amount)")
                redirectAttributes.addFlashAttribute("selectedSourceType", sourceType)
                return "redirect:/upload"
            }
            
            val bookingsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bookings)
            
            redirectAttributes.addFlashAttribute("bookings", bookings)
            redirectAttributes.addFlashAttribute("bookingsJson", bookingsJson)
            redirectAttributes.addFlashAttribute("selectedSourceType", sourceType)
            redirectAttributes.addFlashAttribute("success", true)
            
            // Set source description based on input mode
            val sourceDescription = when (inputMode) {
                "file" -> file?.originalFilename ?: "uploaded file"
                "text" -> "text input (${textContent?.lines()?.count { it.isNotBlank() }} lines)"
                "predefined" -> predefinedFile
                else -> "unknown source"
            }
            redirectAttributes.addFlashAttribute("sourceDescription", sourceDescription)
            
            return "redirect:/upload"
            
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("not found") == true -> "Selected sample file not found. Please try a different file."
                e.message?.contains("cannot be cast") == true -> "Invalid file format. Please upload a text file with comma-separated values."
                e.message?.contains("encoding") == true -> "File encoding issue. Please ensure the file is in UTF-8 format."
                e is IllegalArgumentException -> e.message ?: "Invalid input provided"
                else -> "Error processing data: ${e.message ?: "Unknown error occurred"}"
            }
            
            redirectAttributes.addFlashAttribute("error", errorMessage)
            redirectAttributes.addFlashAttribute("selectedSourceType", sourceType)
            return "redirect:/upload"
        }
    }
}