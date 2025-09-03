package com.divadvo.kotlinspring

data class DirectoryItem(
    val name: String,
    val isDirectory: Boolean,
    val size: Long = 0
)