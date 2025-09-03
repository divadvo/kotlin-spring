package com.divadvo.kotlinspring.model.dto

data class PredefinedFile(
    val filename: String,
    val relativePath: String,
    val displayName: String,
    val recordCount: Int
)