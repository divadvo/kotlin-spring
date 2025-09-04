package com.divadvo.kotlinspring.controller.api

import com.divadvo.kotlinspring.model.domain.Booking
import com.divadvo.kotlinspring.model.dto.BookingTransformRequest
import com.divadvo.kotlinspring.model.enums.SourceType
import com.divadvo.kotlinspring.service.BookingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/transform")
@Tag(name = "Booking Transform", description = "XML to Booking transformation endpoints")
class BookingTransformController(
    private val bookingService: BookingService
) {

    @PostMapping("/booking")
    @Operation(
        summary = "Transform XML to bookings (JSON input)",
        description = "Accepts XML payload and source type as JSON request body, returns list of parsed bookings"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200", 
            description = "Successfully transformed XML to bookings",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = Array<Booking>::class)
            )]
        ),
        ApiResponse(
            responseCode = "400", 
            description = "Invalid XML format or missing required fields"
        )
    )
    fun transformBookingsJson(
        @RequestBody 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Request containing XML payload and source type",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = BookingTransformRequest::class),
                examples = [ExampleObject(
                    name = "Sample booking XML",
                    value = """{
  "xmlPayload": "<bookings><booking><customerName>John Doe</customerName><amount>150.00</amount></booking><booking customerName=\"John Doe\" id=\"123\"></booking></bookings>",
  "sourceType": "A"
}"""
                )]
            )]
        )
        request: BookingTransformRequest
    ): List<Booking> {
        return bookingService.processBookingsFromText(request.xmlPayload, request.sourceType)
    }

    @PostMapping("/booking/form")
    @Operation(
        summary = "Transform XML to bookings (form input)",
        description = "Accepts XML payload and source type as form parameters, returns list of parsed bookings"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200", 
            description = "Successfully transformed XML to bookings",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = Array<Booking>::class)
            )]
        ),
        ApiResponse(
            responseCode = "400", 
            description = "Invalid XML format or missing required parameters"
        )
    )
    fun transformBookingsForm(
        @Parameter(
            description = "XML payload containing booking data",
            example = "<bookings><booking><customerName>Jane Smith</customerName><amount>200.00</amount></booking></bookings>"
        )
        @RequestParam xmlPayload: String,
        
        @Parameter(
            description = "Source type for processing the XML content",
            example = "A"
        )
        @RequestParam sourceType: SourceType
    ): List<Booking> {
        return bookingService.processBookingsFromText(xmlPayload, sourceType)
    }
}