package com.divadvo.kotlinspring

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
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
    
    private val logger = LoggerFactory.getLogger(FileUploadController::class.java)

    @GetMapping("/upload")
    fun uploadForm(model: Model): String {
        addCommonAttributes(model)
        return "upload"
    }
    
    private fun addCommonAttributes(model: Model) {
        model.addAttribute("sourceTypes", SourceType.values())
        model.addAttribute("predefinedFiles", getPredefinedFiles())
    }
    
    private fun getPredefinedFiles(): List<PredefinedFile> {
        return try {
            val resolver = PathMatchingResourcePatternResolver()
            val resources = resolver.getResources("classpath:data/*.csv")
            
            resources.mapNotNull { resource ->
                try {
                    val filename = resource.filename
                    if (filename != null) {
                        val content = resource.inputStream.bufferedReader().readText()
                        val lineCount = content.lines().count { it.isNotBlank() }
                        
                        PredefinedFile(
                            filename = filename,
                            displayName = generateDisplayName(filename),
                            recordCount = lineCount
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }.sortedBy { it.displayName }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun generateDisplayName(filename: String): String {
        val nameWithoutExtension = filename.substringBeforeLast(".")
        return nameWithoutExtension
            .split("-", "_")
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
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
        logger.info("Processing data request - inputMode: $inputMode, sourceType: $sourceType")
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
                logger.warn("No bookings processed from input mode: $inputMode")
                redirectAttributes.addFlashAttribute("error", "No valid booking data found. Please ensure the data contains comma-separated values (customerName,amount)")
                redirectAttributes.addFlashAttribute("selectedSourceType", sourceType)
                return "redirect:/upload"
            }
            
            logger.info("Successfully processed ${bookings.size} bookings from $inputMode")
            val bookingsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bookings)
            
            redirectAttributes.addFlashAttribute("bookings", bookings)
            redirectAttributes.addFlashAttribute("bookingsJson", bookingsJson)
            redirectAttributes.addFlashAttribute("selectedSourceType", sourceType)
            redirectAttributes.addFlashAttribute("selectedInputMode", inputMode)
            redirectAttributes.addFlashAttribute("predefinedFiles", getPredefinedFiles())
            redirectAttributes.addFlashAttribute("sourceTypes", SourceType.values())
            redirectAttributes.addFlashAttribute("success", true)
            
            // Preserve form data based on input mode
            when (inputMode) {
                "text" -> redirectAttributes.addFlashAttribute("preservedTextContent", textContent)
                "predefined" -> redirectAttributes.addFlashAttribute("selectedPredefinedFile", predefinedFile)
            }
            
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
            logger.error("Error processing data request - inputMode: $inputMode, sourceType: $sourceType", e)
            val errorMessage = when {
                e.message?.contains("not found") == true -> "Selected sample file not found. Please try a different file."
                e.message?.contains("cannot be cast") == true -> "Invalid file format. Please upload a text file with comma-separated values."
                e.message?.contains("encoding") == true -> "File encoding issue. Please ensure the file is in UTF-8 format."
                e is IllegalArgumentException -> e.message ?: "Invalid input provided"
                else -> "Error processing data: ${e.message ?: "Unknown error occurred"}"
            }
            
            redirectAttributes.addFlashAttribute("error", errorMessage)
            redirectAttributes.addFlashAttribute("selectedSourceType", sourceType)
            redirectAttributes.addFlashAttribute("selectedInputMode", inputMode)
            redirectAttributes.addFlashAttribute("predefinedFiles", getPredefinedFiles())
            redirectAttributes.addFlashAttribute("sourceTypes", SourceType.values())
            
            // Preserve form data on error too
            when (inputMode) {
                "text" -> redirectAttributes.addFlashAttribute("preservedTextContent", textContent)
                "predefined" -> redirectAttributes.addFlashAttribute("selectedPredefinedFile", predefinedFile)
            }
            
            return "redirect:/upload"
        }
    }
}