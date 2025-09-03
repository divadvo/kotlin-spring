package com.divadvo.kotlinspring.model.dto

data class DirectoryItem(
    val name: String,
    val isDirectory: Boolean,
    val size: Long = 0
)