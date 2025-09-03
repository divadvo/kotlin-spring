package com.divadvo.kotlinspring

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Messages", description = "Simple greeting and message endpoints")
class MessageController {
    
    @GetMapping("/")
    @Operation(summary = "Get personalized greeting", description = "Returns a greeting message with the provided name")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully generated greeting"),
        ApiResponse(responseCode = "400", description = "Invalid name parameter")
    )
    fun index(
        @Parameter(description = "Name to include in greeting", example = "World", required = true)
        @RequestParam("name") name: String
    ) = "Hello, $name!"
    
    @GetMapping("/api/status")
    @Operation(summary = "Get application status", description = "Returns the current status and version of the application")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved status")
    fun getStatus() = mapOf(
        "status" to "running",
        "version" to "1.0.0",
        "timestamp" to System.currentTimeMillis()
    )
}