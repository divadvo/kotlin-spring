package com.divadvo.kotlinspring.service

import com.divadvo.kotlinspring.model.dto.DirectoryBrowseResult
import com.divadvo.kotlinspring.model.dto.DirectoryItem
import com.divadvo.kotlinspring.model.dto.FileReadResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Service
class FileSystemService {
    
    private val logger = LoggerFactory.getLogger(FileSystemService::class.java)
    
    fun browseDirectory(path: String?): DirectoryBrowseResult {
        val directoryPath = path ?: File.listRoots().firstOrNull()?.absolutePath ?: "/"
        logger.info("Browsing directory: $directoryPath")
        
        return try {
            val directory = File(directoryPath)
            if (!directory.exists() || !directory.isDirectory) {
                logger.warn("Directory not found or not a directory: $directoryPath")
                return DirectoryBrowseResult.Error(
                    message = "Directory not found: $directoryPath",
                    currentPath = directoryPath
                )
            }
            
            val items = directory.listFiles()?.map { file ->
                DirectoryItem(
                    name = file.name,
                    isDirectory = file.isDirectory,
                    size = if (file.isFile) file.length() else 0
                )
            }?.sortedWith(compareBy<DirectoryItem> { !it.isDirectory }.thenBy { it.name }) ?: emptyList()
            
            logger.info("Listed ${items.size} items in directory: $directoryPath")
            
            DirectoryBrowseResult.Success(
                items = items,
                currentPath = directoryPath,
                parentPath = directory.parent
            )
            
        } catch (e: Exception) {
            logger.error("Error browsing directory: $directoryPath", e)
            DirectoryBrowseResult.Error(
                message = "Error accessing directory: ${e.message}",
                currentPath = directoryPath
            )
        }
    }
    
    fun readFileContent(path: String?): FileReadResult {
        val filePath = path ?: ""
        logger.info("Reading file: $filePath")
        
        if (filePath.isEmpty()) {
            return FileReadResult.Error(
                message = "No file path provided",
                currentPath = ""
            )
        }
        
        return try {
            val file = File(filePath)
            if (!file.exists() || !file.isFile) {
                logger.warn("File not found or not a file: $filePath")
                return FileReadResult.Error(
                    message = "File not found: $filePath",
                    currentPath = filePath
                )
            }
            
            val content = Files.readString(Paths.get(filePath))
            logger.info("Loaded file content: $filePath (${file.length()} bytes)")
            
            FileReadResult.Success(
                content = content,
                currentPath = filePath,
                parentPath = file.parent,
                fileName = file.name,
                fileSize = file.length()
            )
            
        } catch (e: Exception) {
            logger.error("Error reading file: $filePath", e)
            FileReadResult.Error(
                message = "Error reading file: ${e.message}",
                currentPath = filePath
            )
        }
    }
}