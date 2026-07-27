package com.example.data.model

data class TabItem(
    val id: String,
    val title: String,
    val currentPath: String,
    val history: List<String> = listOf(currentPath),
    val historyIndex: Int = 0
)

data class StorageStats(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val imagesBytes: Long,
    val videosBytes: Long,
    val audioBytes: Long,
    val documentsBytes: Long,
    val archivesBytes: Long,
    val apksBytes: Long,
    val systemBytes: Long
) {
    val usedRatio: Float
        get() = if (totalBytes > 0) usedBytes.toFloat() / totalBytes.toFloat() else 0f
}
