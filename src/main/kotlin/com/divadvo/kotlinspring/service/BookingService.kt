package com.divadvo.kotlinspring.service

import com.divadvo.kotlinspring.model.domain.Booking
import com.divadvo.kotlinspring.model.enums.SourceType
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
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

    fun processBookingsFromPredefinedFile(fileName: String, sourceType: SourceType): List<Booking> {
        logger.info("Processing bookings from predefined file: $fileName, sourceType: $sourceType")
        val resource = ClassPathResource("data/$fileName")
        if (!resource.exists()) {
            logger.error("Predefined file not found: data/$fileName")
            throw IllegalArgumentException("Predefined file '$fileName' not found")
        }

        val content = resource.inputStream.bufferedReader().readText()
        val result = parseCSVContent(content, sourceType)
        logger.info("Successfully processed ${result.size} bookings from predefined file: $fileName")
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
}