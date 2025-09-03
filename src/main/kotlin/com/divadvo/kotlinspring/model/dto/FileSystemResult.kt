package com.divadvo.kotlinspring.model.dto

sealed class DirectoryBrowseResult {
    data class Success(
        val items: List<DirectoryItem>,
        val currentPath: String,
        val parentPath: String?
    ) : DirectoryBrowseResult()
    
    data class Error(
        val message: String,
        val currentPath: String
    ) : DirectoryBrowseResult()
}

sealed class FileReadResult {
    data class Success(
        val content: String,
        val currentPath: String,
        val parentPath: String?,
        val fileName: String,
        val fileSize: Long
    ) : FileReadResult()
    
    data class Error(
        val message: String,
        val currentPath: String
    ) : FileReadResult()
}