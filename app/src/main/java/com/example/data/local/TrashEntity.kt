package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trash_items")
data class TrashEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalPath: String,
    val fileName: String,
    val tempPath: String,
    val sizeBytes: Long,
    val isDirectory: Boolean,
    val deletedAt: Long = System.currentTimeMillis()
)
