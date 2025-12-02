package com.legozia.files

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.legozia.files.ui.AppNavigation
import com.legozia.files.viewmodel.FileManagerViewModel
import com.legozia.files.viewmodel.FileManagerViewModelFactory
import com.legozia.files.viewmodel.ThemeViewModel
import com.legozia.files.ui.theme.LegoziaFilesTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            val systemDarkTheme = isSystemInDarkTheme()
            
            LegoziaFilesTheme(
                darkTheme = isDarkTheme ?: systemDarkTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val factory = FileManagerViewModelFactory(applicationContext)
                    val viewModel: FileManagerViewModel = viewModel(factory = factory)
                    AppNavigation(
                        viewModel = viewModel,
                        themeViewModel = themeViewModel
                    )
                }

            }
        }
    }
}
