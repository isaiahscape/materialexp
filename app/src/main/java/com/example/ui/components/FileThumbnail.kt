package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.ui.theme.ColorApk
import com.example.ui.theme.ColorArchive
import com.example.ui.theme.ColorAudio
import com.example.ui.theme.ColorCode
import com.example.ui.theme.ColorDoc
import com.example.ui.theme.ColorFolder
import com.example.ui.theme.ColorImage
import com.example.ui.theme.ColorVideo
import com.example.utils.ThumbnailUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    if (file.category == FileCategory.IMAGE || file.category == FileCategory.VIDEO) {
        SubcomposeAsyncImage(
            model = file.path,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxSize(),
            error = {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = fallbackIcon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
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

fun getCategoryVisuals(category: FileCategory): Pair<ImageVector, Color> {
    return when (category) {
        FileCategory.FOLDER -> Icons.Default.Folder to ColorFolder
        FileCategory.IMAGE -> Icons.Default.Image to ColorImage
        FileCategory.VIDEO -> Icons.Default.Movie to ColorVideo
        FileCategory.AUDIO -> Icons.Default.AudioFile to ColorAudio
        FileCategory.DOCUMENT -> Icons.Default.Description to ColorDoc
        FileCategory.CODE -> Icons.Default.Code to ColorCode
        FileCategory.ARCHIVE -> Icons.Default.FolderZip to ColorArchive
        FileCategory.APK -> Icons.Default.Android to ColorApk
        else -> Icons.Default.QuestionMark to Color.Gray
    }
}
