package com.divadvo.kotlinspring.service

import com.divadvo.kotlinspring.model.domain.Booking
import com.divadvo.kotlinspring.model.dto.PredefinedFile
import com.divadvo.kotlinspring.model.enums.SourceType
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class BookingService {

    private val logger = LoggerFactory.getLogger(BookingService::class.java)

    fun processBookings(file: MultipartFile, sourceType: SourceType): List<Booking> {
        logger.info("Processing bookings from file: ${file.originalFilename} (${file.size} bytes), sourceType: $sourceType")
        val content = file.inputStream.bufferedReader().readText()
        val result = parseCSVContent(content, sourceType)
        logger.info("Successfully processed ${result.size} bookings from file: ${file.originalFilename}")
        return result
    }

    fun processBookingsFromText(textContent: String, sourceType: SourceType): List<Booking> {
        logger.info("Processing bookings from text input (${textContent.length} characters), sourceType: $sourceType")
        val result = parseCSVContent(textContent, sourceType)
        logger.info("Successfully processed ${result.size} bookings from text input")
        return result
    }

    fun processBookingsFromPredefinedFile(relativePath: String, sourceType: SourceType): List<Booking> {
        logger.info("Processing bookings from predefined file: $relativePath, sourceType: $sourceType")
        val resource = ClassPathResource("data/$relativePath")
        if (!resource.exists()) {
            logger.error("Predefined file not found: data/$relativePath")
            throw IllegalArgumentException("Predefined file '$relativePath' not found")
        }

        val content = resource.inputStream.bufferedReader().readText()
        val result = parseCSVContent(content, sourceType)
        logger.info("Successfully processed ${result.size} bookings from predefined file: $relativePath")
        return result
    }

    private fun parseCSVContent(content: String, sourceType: SourceType): List<Booking> {
        logger.debug("Parsing CSV content with ${content.length} characters")
        val bookings = mutableListOf<Booking>()
        val lines = content.lines().filter { it.isNotBlank() }
        logger.debug("Found ${lines.size} non-blank lines to process")

        var validLines = 0
        var invalidLines = 0

        lines.forEachIndexed { index, line ->
            val parts = line.split(",")
            if (parts.size >= 2) {
                val customerName = parts[0].trim()
                val amountStr = parts.getOrNull(1)?.trim()
                val amount = amountStr?.toDoubleOrNull()

                if (customerName.isNotEmpty() && amount != null) {
                    bookings.add(
                        Booking(
                            id = (index + 1).toLong(),
                            customerName = customerName,
                            bookingDate = LocalDateTime.now(),
                            sourceType = sourceType,
                            amount = amount
                        )
                    )
                    validLines++
                } else {
                    logger.warn("Invalid line ${index + 1}: '$line' - customerName: '$customerName', amount: '$amountStr'")
                    invalidLines++
                }
            } else {
                logger.warn("Skipping line ${index + 1}: '$line' - insufficient fields (${parts.size}/2)")
                invalidLines++
            }
        }

        logger.debug("Parsed $validLines valid bookings, $invalidLines invalid lines")
        return bookings
    }
    
    fun getPredefinedFiles(): List<PredefinedFile> {
        return try {
            val resolver = PathMatchingResourcePatternResolver()
            val resources = resolver.getResources("classpath:data/**/*.csv")
            
            resources.mapNotNull { resource ->
                try {
                    val filename = resource.filename
                    val uri = resource.uri.toString()
                    
                    if (filename != null) {
                        // Extract relative path from the URI (everything after "data/")
                        val relativePath = if (uri.contains("data/")) {
                            uri.substringAfter("data/")
                        } else {
                            filename
                        }
                        
                        val content = resource.inputStream.bufferedReader().readText()
                        val lineCount = content.lines().count { it.isNotBlank() }
                        
                        PredefinedFile(
                            filename = filename,
                            relativePath = relativePath,
                            displayName = generateDisplayName(relativePath),
                            recordCount = lineCount
                        )
                    } else null
                } catch (e: Exception) {
                    logger.warn("Error reading predefined file: ${resource.filename}", e)
                    null
                }
            }.sortedBy { it.displayName }
        } catch (e: Exception) {
            logger.error("Error loading predefined files", e)
            emptyList()
        }
    }
    
    private fun generateDisplayName(relativePath: String): String {
        val pathParts = relativePath.split("/")
        val filename = pathParts.last().substringBeforeLast(".")
        
        // Generate display name for the file part
        val fileDisplayName = filename
            .split("-", "_")
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        
        // If there are folder parts, include them in the display name
        return if (pathParts.size > 1) {
            val folderParts = pathParts.dropLast(1)
            val folderDisplayName = folderParts.joinToString(" / ") { folder ->
                folder.split("-", "_")
                    .joinToString(" ") { word ->
                        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }
            }
            "$folderDisplayName / $fileDisplayName"
        } else {
            fileDisplayName
        }
    }
}