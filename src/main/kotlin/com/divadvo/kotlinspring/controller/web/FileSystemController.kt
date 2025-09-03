package com.divadvo.kotlinspring.controller.web

import com.divadvo.kotlinspring.model.dto.DirectoryBrowseResult
import com.divadvo.kotlinspring.model.dto.FileReadResult
import com.divadvo.kotlinspring.service.FileSystemService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class FileSystemController(
    private val fileSystemService: FileSystemService
) {

    private val logger = LoggerFactory.getLogger(FileSystemController::class.java)

    @GetMapping(value = ["/my-uploader/browse", "/my-uploader/browse/"])
    fun browse(@RequestParam(required = false) path: String?, model: Model): String {
        when (val result = fileSystemService.browseDirectory(path)) {
            is DirectoryBrowseResult.Success -> {
                model.addAttribute("items", result.items)
                model.addAttribute("currentPath", result.currentPath)
                model.addAttribute("parentPath", result.parentPath)
            }
            is DirectoryBrowseResult.Error -> {
                model.addAttribute("error", result.message)
                model.addAttribute("currentPath", result.currentPath)
            }
        }
        return "browse"
    }

    @GetMapping(value = ["/my-uploader/view", "/my-uploader/view/"])
    fun view(@RequestParam(required = false) path: String?, model: Model): String {
        when (val result = fileSystemService.readFileContent(path)) {
            is FileReadResult.Success -> {
                model.addAttribute("content", result.content)
                model.addAttribute("currentPath", result.currentPath)
                model.addAttribute("parentPath", result.parentPath)
                model.addAttribute("fileName", result.fileName)
                model.addAttribute("fileSize", result.fileSize)
            }
            is FileReadResult.Error -> {
                model.addAttribute("error", result.message)
                model.addAttribute("currentPath", result.currentPath)
            }
        }
        return "view"
    }

}