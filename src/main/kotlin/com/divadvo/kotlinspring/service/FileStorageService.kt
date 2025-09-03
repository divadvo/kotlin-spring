package com.divadvo.kotlinspring.service

import com.divadvo.kotlinspring.model.dto.FileStorageResult
import com.divadvo.kotlinspring.model.enums.SourceType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class FileStorageService(
    @Value("\${app.storage.input.path.a}") private val pathA: String,
    @Value("\${app.storage.input.path.b}") private val pathB: String
) {

    private val logger = LoggerFactory.getLogger(FileStorageService::class.java)
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss")

    fun saveFileToFolder(
        file: MultipartFile?, 
        textContent: String?, 
        predefinedFile: String?, 
        inputMode: String, 
        sourceType: SourceType
    ): FileStorageResult {
        val folderPath = when (sourceType) {
            SourceType.A -> pathA
            SourceType.B -> pathB
        }

        val timestamp = LocalDateTime.now().format(timestampFormatter)
        val folder = File(folderPath)
        
        logger.info("Target storage folder: ${folder.absolutePath}")
        
        if (!folder.exists()) {
            logger.info("Directory doesn't exist, creating: ${folder.absolutePath}")
            val created = folder.mkdirs()
            if (created) {
                logger.info("Successfully created directory: ${folder.absolutePath}")
            } else {
                logger.error("Failed to create directory: ${folder.absolutePath}")
                throw IllegalStateException("Unable to create storage directory: ${folder.absolutePath}")
            }
        } else {
            logger.info("Directory already exists: ${folder.absolutePath}")
        }
        
        // Check if directory is writable
        if (!folder.canWrite()) {
            logger.error("Directory is not writable: ${folder.absolutePath}")
            throw IllegalStateException("Storage directory is not writable: ${folder.absolutePath}")
        }

        val fileName = when (inputMode) {
            "file" -> {
                val originalName = file?.originalFilename ?: "unknown"
                val extension = if (originalName.contains(".")) originalName.substringAfterLast(".") else "txt"
                val baseName = if (originalName.contains(".")) originalName.substringBeforeLast(".") else originalName
                "${timestamp}_${baseName}.${extension}"
            }
            "text" -> "${timestamp}_text-input.txt"
            "predefined" -> {
                val originalName = predefinedFile ?: "sample"
                val extension = if (originalName.contains(".")) originalName.substringAfterLast(".") else "csv"
                val baseName = if (originalName.contains(".")) originalName.substringBeforeLast(".").replace("/", "_") else originalName
                "${timestamp}_${baseName}.${extension}"
            }
            else -> "${timestamp}_data.txt"
        }

        val targetFile = File(folder, fileName)
        logger.info("Target file path: ${targetFile.absolutePath}")
        
        try {
            when (inputMode) {
                "file" -> {
                    if (file != null && !file.isEmpty) {
                        logger.info("Transferring uploaded file (${file.size} bytes) to: ${targetFile.absolutePath}")
                        file.transferTo(targetFile)
                        logger.info("Successfully saved uploaded file to: ${targetFile.absolutePath}")
                    } else {
                        throw IllegalArgumentException("No file provided for file input mode")
                    }
                }
                "text" -> {
                    if (!textContent.isNullOrBlank()) {
                        logger.info("Writing text content (${textContent.length} characters) to: ${targetFile.absolutePath}")
                        targetFile.writeText(textContent)
                        logger.info("Successfully saved text content to: ${targetFile.absolutePath}")
                    } else {
                        throw IllegalArgumentException("No text content provided")
                    }
                }
                "predefined" -> {
                    if (!predefinedFile.isNullOrBlank()) {
                        logger.info("Copying predefined file '$predefinedFile' to: ${targetFile.absolutePath}")
                        val sourceFile = this::class.java.getResourceAsStream("/data/$predefinedFile")
                            ?: throw IllegalArgumentException("Predefined file not found: $predefinedFile")
                        
                        sourceFile.use { input ->
                            targetFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        logger.info("Successfully saved predefined file to: ${targetFile.absolutePath}")
                    } else {
                        throw IllegalArgumentException("No predefined file specified")
                    }
                }
                else -> throw IllegalArgumentException("Unknown input mode: $inputMode")
            }
        } catch (e: Exception) {
            logger.error("Failed to save file to: ${targetFile.absolutePath}", e)
            throw IllegalStateException("Failed to save file: ${e.message}", e)
        }

        return FileStorageResult(
            filePath = targetFile.absolutePath,
            folderPath = folder.absolutePath
        )
    }
}