package com.overseer.explorer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.overseer.explorer.data.model.StorageStats
import com.overseer.explorer.ui.components.StorageOverviewCard
import java.util.Locale
import com.overseer.explorer.data.model.FileCategory
import com.overseer.explorer.ui.components.getCategoryVisuals

@Composable
fun StorageAnalyzerScreen(
    stats: StorageStats?,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (stats == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Calculating Storage Statistics...")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Storage Overview",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Internal Shared Storage Analyzer",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large Usage Progress Meter Card
        StorageOverviewCard(stats = stats)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Breakdown by Category",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(12.dp))

        CategoryRow("Images", stats.imagesBytes, FileCategory.IMAGE) { onCategoryClick("IMAGE") }
        CategoryRow("Videos", stats.videosBytes, FileCategory.VIDEO) { onCategoryClick("VIDEO") }
        CategoryRow("Audio & Music", stats.audioBytes, FileCategory.AUDIO) { onCategoryClick("AUDIO") }
        CategoryRow("Documents", stats.documentsBytes, FileCategory.DOCUMENT) { onCategoryClick("DOCUMENT") }
        CategoryRow("Archives & ZIPs", stats.archivesBytes, FileCategory.ARCHIVE) { onCategoryClick("ARCHIVE") }
        CategoryRow("APKs & Apps", stats.apksBytes, FileCategory.APK) { onCategoryClick("APK") }
        CategoryRow("System & OS Data", stats.systemBytes, FileCategory.UNKNOWN) {}
    }
}

@Composable
private fun CategoryRow(
    title: String,
    bytes: Long,
    category: FileCategory,
    onClick: () -> Unit
) {
    val (icon, color) = getCategoryVisuals(category)
    val mb = bytes / (1024.0 * 1024.0)
    val gb = mb / 1024.0
    val formatted = if (gb >= 1.0) String.format(Locale.getDefault(), "%.2f GB", gb) else String.format(Locale.getDefault(), "%.1f MB", mb)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = formatted,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}
