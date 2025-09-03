package com.divadvo.kotlinspring

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class BookingService {
    
    fun processBookings(file: MultipartFile, sourceType: SourceType): List<Booking> {
        val bookings = mutableListOf<Booking>()
        
        try {
            val content = file.inputStream.bufferedReader().readText()
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
        } catch (e: Exception) {

        }
        
        return bookings
    }
}