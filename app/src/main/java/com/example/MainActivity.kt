package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.ExplorerMainScreen
import com.example.ui.theme.CleanExplorerTheme
import com.example.ui.viewmodel.ExplorerViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ExplorerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanExplorerTheme {
                ExplorerMainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkStoragePermission()
    }
}
