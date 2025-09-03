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
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory
import java.io.StringReader
import org.xml.sax.InputSource

@Service
class BookingService {

    private val logger = LoggerFactory.getLogger(BookingService::class.java)

    fun processBookings(file: MultipartFile, sourceType: SourceType): List<Booking> {
        logger.info("Processing bookings from file: ${file.originalFilename} (${file.size} bytes), sourceType: $sourceType")
        val content = file.inputStream.bufferedReader().readText()
        val result = parseXMLContent(content, sourceType)
        logger.info("Successfully processed ${result.size} bookings from file: ${file.originalFilename}")
        return result
    }

    fun processBookingsFromText(textContent: String, sourceType: SourceType): List<Booking> {
        logger.info("Processing bookings from text input (${textContent.length} characters), sourceType: $sourceType")
        val result = parseXMLContent(textContent, sourceType)
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
        val result = parseXMLContent(content, sourceType)
        logger.info("Successfully processed ${result.size} bookings from predefined file: $relativePath")
        return result
    }

    private fun parseXMLContent(content: String, sourceType: SourceType): List<Booking> {
        logger.debug("Parsing XML content with ${content.length} characters")
        val bookings = mutableListOf<Booking>()

        try {
            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()
            val document = documentBuilder.parse(InputSource(StringReader(content)))
            
            val bookingNodes = document.getElementsByTagName("booking")
            logger.debug("Found ${bookingNodes.length} booking elements to process")

            var validBookings = 0
            var invalidBookings = 0

            for (i in 0 until bookingNodes.length) {
                val bookingNode = bookingNodes.item(i)
                if (bookingNode.nodeType == Node.ELEMENT_NODE) {
                    val bookingElement = bookingNode as Element
                    
                    val customerNameElement = bookingElement.getElementsByTagName("customerName").item(0)
                    val amountElement = bookingElement.getElementsByTagName("amount").item(0)
                    
                    if (customerNameElement != null && amountElement != null) {
                        val customerName = customerNameElement.textContent?.trim()
                        val amountStr = amountElement.textContent?.trim()
                        val amount = amountStr?.toDoubleOrNull()

                        if (!customerName.isNullOrEmpty() && amount != null) {
                            bookings.add(
                                Booking(
                                    id = (i + 1).toLong(),
                                    customerName = customerName,
                                    bookingDate = LocalDateTime.now(),
                                    sourceType = sourceType,
                                    amount = amount
                                )
                            )
                            validBookings++
                        } else {
                            logger.warn("Invalid booking ${i + 1}: customerName: '$customerName', amount: '$amountStr'")
                            invalidBookings++
                        }
                    } else {
                        logger.warn("Skipping booking ${i + 1}: missing customerName or amount elements")
                        invalidBookings++
                    }
                }
            }

            logger.debug("Parsed $validBookings valid bookings, $invalidBookings invalid bookings")
        } catch (e: Exception) {
            logger.error("Error parsing XML content", e)
            throw IllegalArgumentException("Invalid XML format: ${e.message}")
        }

        return bookings
    }
    
    fun getPredefinedFiles(): List<PredefinedFile> {
        return try {
            val resolver = PathMatchingResourcePatternResolver()
            val resources = resolver.getResources("classpath:data/**/*.xml")
            
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
                        
                        PredefinedFile(
                            filename = filename,
                            relativePath = relativePath,
                            displayName = generateDisplayName(relativePath)
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