package com.example.data.model

enum class ArchiveFormat(val extension: String, val label: String) {
    ZIP(".zip", "ZIP"),
    SEVEN_Z(".7z", "7-Zip"),
    TAR(".tar", "TAR"),
    TAR_GZ(".tar.gz", "TAR GZip"),
    TAR_BZ2(".tar.bz2", "TAR BZip2"),
    TAR_XZ(".tar.xz", "TAR XZ")
}

enum class CompressionLevel(val level: Int, val label: String) {
    NONE(0, "None"),
    FAST(1, "Fast"),
    NORMAL(5, "Normal"),
    BEST(9, "Best")
}

data class ArchiveOptions(
    val format: ArchiveFormat = ArchiveFormat.ZIP,
    val level: CompressionLevel = CompressionLevel.NORMAL
)
