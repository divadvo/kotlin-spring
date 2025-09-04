package com.divadvo.kotlinspring.model.dto

import com.divadvo.kotlinspring.model.enums.SourceType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request for transforming XML content to booking list")
data class BookingTransformRequest(
    @Schema(
        description = """XML payload containing booking data. 
    IMPORTANT: Quotes within the XML must be escaped with backslashes.""",
        example = """<bookings>
  <booking>
    <customerName>John Doe</customerName>
    <amount>150.00</amount>
  </booking>
</bookings>"""
    )
    val xmlPayload: String,
    
    @Schema(
        description = "Source type for processing the XML content",
        example = "A"
    )
    val sourceType: SourceType
)