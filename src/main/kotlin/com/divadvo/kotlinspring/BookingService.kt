package com.divadvo.kotlinspring

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class BookingService {
    
    fun processBookings(file: MultipartFile, sourceType: SourceType): List<Booking> {
        val content = file.inputStream.bufferedReader().readText()
        return parseCSVContent(content, sourceType)
    }
    
    fun processBookingsFromText(textContent: String, sourceType: SourceType): List<Booking> {
        return parseCSVContent(textContent, sourceType)
    }
    
    fun processBookingsFromPredefinedFile(fileName: String, sourceType: SourceType): List<Booking> {
        val resource = ClassPathResource("data/$fileName")
        if (!resource.exists()) {
            throw IllegalArgumentException("Predefined file '$fileName' not found")
        }
        
        val content = resource.inputStream.bufferedReader().readText()
        return parseCSVContent(content, sourceType)
    }
    
    private fun parseCSVContent(content: String, sourceType: SourceType): List<Booking> {
        val bookings = mutableListOf<Booking>()
        val lines = content.lines().filter { it.isNotBlank() }
        
        lines.forEachIndexed { index, line ->
            val parts = line.split(",")
            if (parts.size >= 2) {
                val customerName = parts[0].trim()
                val amount = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0
                
                bookings.add(
                    Booking(
                        id = (index + 1).toLong(),
                        customerName = customerName,
                        bookingDate = LocalDateTime.now(),
                        sourceType = sourceType,
                        amount = amount
                    )
                )
            }
        }
        
        return bookings
    }
}