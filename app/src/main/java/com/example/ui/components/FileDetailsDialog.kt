package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FileItem

import android.text.format.Formatter
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.utils.RelativeTimeUtils

@Composable
fun FileDetailsDialog(
    file: FileItem,
    hashes: Map<String, String>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showChecksums by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1C1C1E), // Darker gray/black like MiXplorer
            contentColor = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (icon, color) = getCategoryVisuals(file.category)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        FileThumbnail(file, icon, color, modifier = Modifier.size(32.dp))
                    }
                    
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (showChecksums) {
                    ChecksumView(hashes = hashes, onBack = { showChecksums = false }, context = context)
                } else {
                    MainDetailsView(file = file, onShowChecksums = { showChecksums = true }, context = context)
                }
            }
        }
    }
}

@Composable
private fun MainDetailsView(
    file: FileItem,
    onShowChecksums: () -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        InfoRow("Path", file.path)
        InfoRow("Content URI", "content://${context.packageName}.fileprovider/...")
        
        val sizeStr = Formatter.formatFileSize(context, file.sizeBytes)
        InfoRow("Size", "$sizeStr (${String.format(java.util.Locale.getDefault(), "%,d", file.sizeBytes)} B)")
        InfoRow("Used", "$sizeStr (${String.format(java.util.Locale.getDefault(), "%,d", file.sizeBytes)} B)\nEffective: $sizeStr")

        InfoRow("Modified", "${RelativeTimeUtils.formatFullDate(file.lastModified)}\n${RelativeTimeUtils.formatRelativeTime(file.lastModified)}", valueColor = Color(0xFF64B5F6))
        InfoRow("Changed", RelativeTimeUtils.formatFullDate(file.lastChanged))
        InfoRow("Accessed", RelativeTimeUtils.formatFullDate(file.lastAccessed))
        
        InfoRow("Type", if (file.isDirectory) "Directory" else file.mimeType)
        InfoRow("Hidden", if (file.isHidden) "Yes" else "No")
        InfoRow("Permissions", file.permissions)
        
        InfoRow("Metadata", "Device: /storage/emulated\n${if (file.isDirectory) "${file.childCount} items" else ""}")

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
        
        Text(
            text = "Comment",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(vertical = 12.dp)
        )

        Text(
            text = "CHECKSUM",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF64B5F6),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowChecksums() }
                .padding(vertical = 12.dp)
        )
    }
}

@Composable
private fun ChecksumView(
    hashes: Map<String, String>,
    onBack: () -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Tap to copy to clipboard or long-press to compare\nwith the clipboard hash.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        hashes.forEach { (type, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText(type, value))
                        Toast.makeText(context, "$type copied", Toast.LENGTH_SHORT).show()
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$type:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(80.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    color = Color(0xFF64B5F6),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Copy",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF64B5F6),
            modifier = Modifier
                .clickable {
                    val allHashes = hashes.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("All Hashes", allHashes))
                    Toast.makeText(context, "All hashes copied", Toast.LENGTH_SHORT).show()
                }
                .padding(vertical = 12.dp)
        )
        
        Text(
            text = "Back",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray,
            modifier = Modifier
                .clickable { onBack() }
                .padding(vertical = 12.dp)
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.LightGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.width(100.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            modifier = Modifier.weight(1f)
        )
    }
}
