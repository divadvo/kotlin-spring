package com.divadvo.kotlinspring.controller.web

import com.divadvo.kotlinspring.service.BookingService
import com.divadvo.kotlinspring.service.FileStorageService
import com.divadvo.kotlinspring.model.dto.PredefinedFile
import com.divadvo.kotlinspring.model.enums.SourceType
import com.divadvo.kotlinspring.model.enums.ProcessingMode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@Profile("!prod")
class FileUploadController(
    private val bookingService: BookingService,
    private val fileStorageService: FileStorageService,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(FileUploadController::class.java)

    @GetMapping("/my-uploader/upload")
    fun uploadForm(model: Model): String {
        addCommonAttributes(model)
        return "upload"
    }

    private fun addCommonAttributes(model: Model) {
        model.addAttribute("sourceTypes", SourceType.values())
        model.addAttribute("processingModes", ProcessingMode.values())
        model.addAttribute("predefinedFiles", bookingService.getPredefinedFiles())
    }



    @PostMapping(value = ["/my-uploader/upload", "/my-uploader/upload/"])
    fun handleDataProcessing(
        @RequestParam("inputMode") inputMode: String,
        @RequestParam("sourceType") sourceType: SourceType,
        @RequestParam("processingMode") processingMode: ProcessingMode,
        @RequestParam(value = "file", required = false) file: MultipartFile?,
        @RequestParam(value = "textContent", required = false) textContent: String?,
        @RequestParam(value = "predefinedFile", required = false) predefinedFile: String?,
        redirectAttributes: RedirectAttributes
    ): String {
        logger.info("Processing data request - inputMode: $inputMode, sourceType: $sourceType, processingMode: $processingMode")
        try {
            when (processingMode) {
                ProcessingMode.PROCESS -> {
                    val bookings = when (inputMode) {
                        "file" -> {
                            if (file == null || file.isEmpty) {
                                throw IllegalArgumentException("Please select a file to upload")
                            }
                            bookingService.processBookings(file, sourceType)
                        }
                        "text" -> {
                            if (textContent.isNullOrBlank()) {
                                throw IllegalArgumentException("Please enter XML content")
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
                        redirectAttributes.addFlashAttribute("error", "No valid booking data found. Please ensure the data contains valid XML format with booking elements")
                        redirectAttributes.addFlashAttribute("selectedSourceType", sourceType)
                        redirectAttributes.addFlashAttribute("selectedProcessingMode", processingMode)
                        return "redirect:/my-uploader/upload"
                    }

                    logger.info("Successfully processed ${bookings.size} bookings from $inputMode")
                    val bookingsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bookings)

                    redirectAttributes.addFlashAttribute("bookings", bookings)
                    redirectAttributes.addFlashAttribute("bookingsJson", bookingsJson)
                }
                
                ProcessingMode.SAVE_TO_FOLDER -> {
                    val result = fileStorageService.saveFileToFolder(file, textContent, predefinedFile, inputMode, sourceType)
                    logger.info("Successfully saved file to: ${result.filePath}")
                    redirectAttributes.addFlashAttribute("savedFilePath", result.filePath)
                    redirectAttributes.addFlashAttribute("savedFolderPath", result.folderPath)
                }
                
                ProcessingMode.SIMULATE_MFT -> {
                    logger.info("MFT simulation requested - not implemented")
                    redirectAttributes.addFlashAttribute("simulateMft", true)
                }
            }

            // Common attributes for all processing modes
            redirectAttributes.addFlashAttribute("selectedSourceType", sourceType)
            redirectAttributes.addFlashAttribute("selectedInputMode", inputMode)
            redirectAttributes.addFlashAttribute("selectedProcessingMode", processingMode)
            redirectAttributes.addFlashAttribute("predefinedFiles", bookingService.getPredefinedFiles())
            redirectAttributes.addFlashAttribute("sourceTypes", SourceType.values())
            redirectAttributes.addFlashAttribute("processingModes", ProcessingMode.values())
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

            return "redirect:/my-uploader/upload"

        } catch (e: Exception) {
            logger.error("Error processing data request - inputMode: $inputMode, sourceType: $sourceType, processingMode: $processingMode", e)
            val errorMessage = when {
                e.message?.contains("not found") == true -> "Selected sample file not found. Please try a different file."
                e.message?.contains("cannot be cast") == true -> "Invalid file format. Please upload a valid XML file."
                e.message?.contains("encoding") == true -> "File encoding issue. Please ensure the file is in UTF-8 format."
                e is IllegalArgumentException -> e.message ?: "Invalid input provided"
                else -> "Error processing data: ${e.message ?: "Unknown error occurred"}"
            }

            redirectAttributes.addFlashAttribute("error", errorMessage)
            redirectAttributes.addFlashAttribute("selectedSourceType", sourceType)
            redirectAttributes.addFlashAttribute("selectedInputMode", inputMode)
            redirectAttributes.addFlashAttribute("selectedProcessingMode", processingMode)
            redirectAttributes.addFlashAttribute("predefinedFiles", bookingService.getPredefinedFiles())
            redirectAttributes.addFlashAttribute("sourceTypes", SourceType.values())
            redirectAttributes.addFlashAttribute("processingModes", ProcessingMode.values())

            // Preserve form data on error too
            when (inputMode) {
                "text" -> redirectAttributes.addFlashAttribute("preservedTextContent", textContent)
                "predefined" -> redirectAttributes.addFlashAttribute("selectedPredefinedFile", predefinedFile)
            }

            return "redirect:/my-uploader/upload"
        }
    }
}