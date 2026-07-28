package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.SortMode
import com.example.ui.viewmodel.ViewMode
import java.io.File

@Composable
fun BreadcrumbBar(
    currentPath: String,
    rootStoragePath: String,
    isBookmarked: Boolean,
    isDualPane: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToSegment: (String) -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleDualPane: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val segments = remember(currentPath, rootStoragePath) {
        if (currentPath == "/") {
            listOf("/ (Root)" to "/")
        } else if (currentPath.startsWith("/") && !currentPath.startsWith(rootStoragePath)) {
            val parts = currentPath.split('/').filter { it.isNotEmpty() }
            val result = mutableListOf("/ (Root)" to "/")
            var accum = ""
            for (part in parts) {
                accum = "$accum/$part"
                result.add(part to accum)
            }
            result
        } else {
            val relPath = currentPath.removePrefix(rootStoragePath).trim('/')
            if (relPath.isEmpty()) {
                listOf("Internal Storage" to rootStoragePath)
            } else {
                val parts = relPath.split('/')
                val result = mutableListOf("Internal Storage" to rootStoragePath)
                var accum = rootStoragePath
                for (part in parts) {
                    accum = "$accum/$part"
                    result.add(part to accum)
                }
                result
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("btn_navigate_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Path Breadcrumbs
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                segments.forEachIndexed { index, (name, path) ->
                    val isLast = index == segments.lastIndex
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isLast) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable { onNavigateToSegment(path) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = if (isLast) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!isLast) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp).padding(horizontal = 2.dp)
                        )
                    }
                }
            }

            // Quick Actions
            IconButton(onClick = onOpenSearch, modifier = Modifier.testTag("btn_search")) {
                Icon(Icons.Default.Search, contentDescription = "Search Files", tint = MaterialTheme.colorScheme.onSurface)
            }

            IconButton(onClick = onToggleBookmark, modifier = Modifier.testTag("btn_bookmark")) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onToggleDualPane, modifier = Modifier.testTag("btn_dual_pane")) {
                Icon(
                    imageVector = Icons.Default.Splitscreen,
                    contentDescription = "Toggle Dual Pane",
                    tint = if (isDualPane) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

        }
    }
}
