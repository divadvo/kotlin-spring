package com.divadvo.kotlinspring.service

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
    ): String {
        val folderPath = when (sourceType) {
            SourceType.A -> pathA
            SourceType.B -> pathB
        }

        val timestamp = LocalDateTime.now().format(timestampFormatter)
        val folder = File(folderPath)
        
        if (!folder.exists()) {
            folder.mkdirs()
            logger.info("Created directory: ${folder.absolutePath}")
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
        
        when (inputMode) {
            "file" -> {
                if (file != null && !file.isEmpty) {
                    file.transferTo(targetFile)
                    logger.info("Saved uploaded file to: ${targetFile.absolutePath}")
                } else {
                    throw IllegalArgumentException("No file provided for file input mode")
                }
            }
            "text" -> {
                if (!textContent.isNullOrBlank()) {
                    targetFile.writeText(textContent)
                    logger.info("Saved text content to: ${targetFile.absolutePath}")
                } else {
                    throw IllegalArgumentException("No text content provided")
                }
            }
            "predefined" -> {
                if (!predefinedFile.isNullOrBlank()) {
                    val sourceFile = this::class.java.getResourceAsStream("/data/$predefinedFile")
                        ?: throw IllegalArgumentException("Predefined file not found: $predefinedFile")
                    
                    sourceFile.use { input ->
                        targetFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    logger.info("Saved predefined file to: ${targetFile.absolutePath}")
                } else {
                    throw IllegalArgumentException("No predefined file specified")
                }
            }
            else -> throw IllegalArgumentException("Unknown input mode: $inputMode")
        }

        return targetFile.absolutePath
    }
}