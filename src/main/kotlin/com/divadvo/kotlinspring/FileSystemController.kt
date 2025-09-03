package com.divadvo.kotlinspring

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Controller
class FileSystemController {
    
    private val logger = LoggerFactory.getLogger(FileSystemController::class.java)

    @GetMapping(value = ["/my-uploader/browse", "/my-uploader/browse/"])
    fun browse(@RequestParam(required = false) path: String?, model: Model): String {
        val directoryPath = path ?: System.getProperty("user.home")
        logger.info("Browsing directory: $directoryPath")
        
        try {
            val directory = File(directoryPath)
            if (!directory.exists() || !directory.isDirectory) {
                model.addAttribute("error", "Directory not found: $directoryPath")
                model.addAttribute("currentPath", directoryPath)
                return "browse"
            }
            
            val items = directory.listFiles()?.map { file ->
                DirectoryItem(
                    name = file.name,
                    isDirectory = file.isDirectory,
                    size = if (file.isFile) file.length() else 0
                )
            }?.sortedWith(compareBy<DirectoryItem> { !it.isDirectory }.thenBy { it.name }) ?: emptyList()
            
            model.addAttribute("items", items)
            model.addAttribute("currentPath", directoryPath)
            model.addAttribute("parentPath", directory.parent)
            
            logger.info("Listed ${items.size} items in directory: $directoryPath")
            
        } catch (e: Exception) {
            logger.error("Error browsing directory: $directoryPath", e)
            model.addAttribute("error", "Error accessing directory: ${e.message}")
            model.addAttribute("currentPath", directoryPath)
        }
        
        return "browse"
    }
    
    @GetMapping(value = ["/my-uploader/view", "/my-uploader/view/"])
    fun view(@RequestParam(required = false) path: String?, model: Model): String {
        val filePath = path ?: ""
        logger.info("Viewing file: $filePath")
        
        if (filePath.isEmpty()) {
            model.addAttribute("currentPath", "")
            return "view"
        }
        
        try {
            val file = File(filePath)
            if (!file.exists() || !file.isFile) {
                model.addAttribute("error", "File not found: $filePath")
                model.addAttribute("currentPath", filePath)
                return "view"
            }
            
            val content = Files.readString(Paths.get(filePath))
            model.addAttribute("content", content)
            model.addAttribute("currentPath", filePath)
            model.addAttribute("parentPath", file.parent)
            model.addAttribute("fileName", file.name)
            model.addAttribute("fileSize", file.length())
            
            logger.info("Loaded file content: $filePath (${file.length()} bytes)")
            
        } catch (e: Exception) {
            logger.error("Error reading file: $filePath", e)
            model.addAttribute("error", "Error reading file: ${e.message}")
            model.addAttribute("currentPath", filePath)
        }
        
        return "view"
    }
    
    @GetMapping(value = ["/my-uploader/logs", "/my-uploader/logs/"])
    fun logs(model: Model): String {
        logger.info("Accessing Logs page")
        return "logs"
    }
    
    @GetMapping(value = ["/my-uploader/db", "/my-uploader/db/"])
    fun db(model: Model): String {
        logger.info("Accessing DB Viewer page")
        return "db"
    }
}