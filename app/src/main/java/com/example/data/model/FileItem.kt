package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FileCategory {
    ALL, FOLDER, IMAGE, VIDEO, AUDIO, DOCUMENT, ARCHIVE, APK, CODE, UNKNOWN
}

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0L,
    val lastModified: Long = System.currentTimeMillis(),
    val lastAccessed: Long = lastModified,
    val lastChanged: Long = lastModified,
    val category: FileCategory = FileCategory.UNKNOWN,
    val mimeType: String = "application/octet-stream",
    val childCount: Int = 0,
    val isHidden: Boolean = false,
    val isBookmarked: Boolean = false,
    val permissions: String = "rw-r--r--",
    val extension: String = "",
    val isSelected: Boolean = false
) {
    val formattedSize: String
        get() {
            if (isDirectory) return "$childCount items"
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.getDefault(), "%.2f GB", gb)
                mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
                kb >= 1.0 -> String.format(Locale.getDefault(), "%.1f KB", kb)
                else -> "$sizeBytes B"
            }
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }

    companion object {
        fun resolveCategory(name: String, isDirectory: Boolean): FileCategory {
            if (isDirectory) return FileCategory.FOLDER
            val ext = name.substringAfterLast('.', "").lowercase()
            return when (ext) {
                "jpg", "jpeg", "png", "webp", "gif", "svg", "bmp", "dng", "nef", "cr2", "arw" -> FileCategory.IMAGE
                "mp4", "mkv", "webm", "avi", "mov", "3gp" -> FileCategory.VIDEO
                "mp3", "wav", "flac", "aac", "m4a", "ogg" -> FileCategory.AUDIO
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt" -> FileCategory.DOCUMENT
                "zip", "tar", "gz", "7z", "rar", "bz2" -> FileCategory.ARCHIVE
                "apk", "aab" -> FileCategory.APK
                "kt", "java", "json", "xml", "html", "css", "js", "ts", "py", "sh", "c", "cpp" -> FileCategory.CODE
                else -> FileCategory.UNKNOWN
            }
        }
    }
}
