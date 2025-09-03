package com.divadvo.kotlinspring

import java.time.LocalDateTime

data class Booking(
    val id: Long,
    val customerName: String,
    val bookingDate: LocalDateTime,
    val sourceType: SourceType,
    val amount: Double
)