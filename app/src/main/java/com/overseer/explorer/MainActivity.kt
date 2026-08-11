package com.overseer.explorer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.overseer.explorer.ui.screens.ExplorerMainScreen
import com.overseer.explorer.ui.theme.CleanExplorerTheme
import com.overseer.explorer.ui.viewmodel.ExplorerViewModel
import com.overseer.explorer.ui.viewmodel.ThemeMode

class MainActivity : ComponentActivity() {
    private val viewModel: ExplorerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle external intent if present
        intent?.let { viewModel.handleExternalIntent(it) }

        setContent {
            val state by viewModel.uiState.collectAsState()
            val darkTheme = when (state.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            CleanExplorerTheme(darkTheme = darkTheme) {
                ExplorerMainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.handleExternalIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkStoragePermission()
    }
}
