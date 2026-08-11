package com.overseer.explorer.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.overseer.explorer.data.model.FileCategory
import com.overseer.explorer.data.model.FileItem
import com.overseer.explorer.ui.theme.*
import com.overseer.explorer.utils.ThumbnailUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun FileThumbnail(
    file: FileItem,
    fallbackIcon: ImageVector,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val thumbnail by produceState<Bitmap?>(initialValue = null, file.path) {
        value = withContext(Dispatchers.IO) {
            when (file.category) {
                FileCategory.APK -> ThumbnailUtils.getApkIcon(context, file.path)
                FileCategory.AUDIO -> ThumbnailUtils.getAudioAlbumArt(file.path)
                else -> null
            }
        }
    }

    val folderPreviewPath by produceState<String?>(initialValue = null, file.path) {
        if (file.isDirectory) {
            value = withContext(Dispatchers.IO) {
                ThumbnailUtils.getFolderPreview(file.path)
            }
        }
    }

    val placeholderPainter = rememberVectorPainter(image = fallbackIcon)

    if (file.category == FileCategory.IMAGE || file.category == FileCategory.VIDEO) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(file.path)
                .crossfade(true)
                .size(128)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = placeholderPainter,
            error = placeholderPainter,
            modifier = modifier.fillMaxSize()
        )
    } else if (folderPreviewPath != null) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(folderPreviewPath)
                .crossfade(true)
                .size(128)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = placeholderPainter,
            error = placeholderPainter,
            modifier = modifier.fillMaxSize()
        )
    } else if (thumbnail != null) {
        AsyncImage(
            model = thumbnail,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier.size(32.dp)
        )
    } else {
        Icon(
            imageVector = fallbackIcon,
            contentDescription = null,
            tint = badgeColor,
            modifier = modifier.size(24.dp)
        )
    }
}

@Composable
fun getCategoryVisuals(category: FileCategory): Pair<ImageVector, Color> {
    val isDark = isSystemInDarkTheme()
    return when (category) {
        FileCategory.FOLDER -> Icons.Default.Folder to (if (isDark) ColorFolderDark else ColorFolderLight)
        FileCategory.IMAGE -> Icons.Default.Image to (if (isDark) ColorImageDark else ColorImageLight)
        FileCategory.VIDEO -> Icons.Default.Movie to (if (isDark) ColorVideoDark else ColorVideoLight)
        FileCategory.AUDIO -> Icons.Default.AudioFile to (if (isDark) ColorAudioDark else ColorAudioLight)
        FileCategory.DOCUMENT -> Icons.Default.Description to (if (isDark) ColorDocDark else ColorDocLight)
        FileCategory.CODE -> Icons.Default.Code to (if (isDark) ColorCodeDark else ColorCodeLight)
        FileCategory.ARCHIVE -> Icons.Default.FolderZip to (if (isDark) ColorArchiveDark else ColorArchiveLight)
        FileCategory.APK -> Icons.Default.Android to (if (isDark) ColorApkDark else ColorApkLight)
        else -> Icons.Default.QuestionMark to Color.Gray
    }
}
