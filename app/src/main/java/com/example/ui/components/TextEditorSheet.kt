package com.example.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FileItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorSheet(
    file: FileItem,
    content: String,
    isModified: Boolean,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isHtmlOrWeb = remember(file.name) {
        val ext = file.name.substringAfterLast('.', "").lowercase()
        ext in listOf("html", "htm", "xml", "svg", "css", "js", "json")
    }

    val quickSymbols = listOf("<", ">", "/", "\"", "=", "{", "}", "(", ")", ";", "div", "p", "script", "style", "class=", "id=")

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = file.name + if (isModified) " *" else "",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = file.path,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onClose, modifier = Modifier.testTag("btn_close_editor")) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Close Editor")
                            }
                        },
                        actions = {
                            Button(
                                onClick = onSave,
                                enabled = isModified,
                                modifier = Modifier.padding(end = 8.dp).testTag("btn_save_editor"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                                Text("Save")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Code, contentDescription = null) },
                            text = { Text("Code Editor") },
                            modifier = Modifier.testTag("tab_code_editor")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Language, contentDescription = null) },
                            text = { Text(if (isHtmlOrWeb) "HTML Live Preview" else "Web Preview") },
                            modifier = Modifier.testTag("tab_html_preview")
                        )
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        if (selectedTab == 0) {
                            // Quick Symbol Keyboard Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B))
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                quickSymbols.forEach { symbol ->
                                    Surface(
                                        onClick = { onContentChange(content + symbol) },
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = symbol,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFF8FAFC)
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val lineCount = content.lines().size
                            val charCount = content.length
                            Text(
                                text = "Lines: $lineCount | Characters: $charCount | UTF-8 | ${file.name.substringAfterLast('.', "TXT").uppercase()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF0F172A))
            ) {
                if (selectedTab == 0) {
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Line numbers sidebar
                        val lineCount = content.lines().size
                        val lineNumbersText = (1..lineCount).joinToString("\n")
                        Text(
                            text = lineNumbersText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            ),
                            modifier = Modifier
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 10.dp, vertical = 16.dp)
                                .verticalScroll(rememberScrollState())
                        )

                        // Text Editor Body
                        OutlinedTextField(
                            value = content,
                            onValueChange = onContentChange,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("input_code_editor"),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = Color(0xFFF8FAFC)
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                } else {
                    // HTML / Web Live Preview Mode
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowFileAccess = true
                                loadDataWithBaseURL(null, content, "text/html", "UTF-8", null)
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize().testTag("webview_html_preview")
                    )
                }
            }
        }
    }
}
