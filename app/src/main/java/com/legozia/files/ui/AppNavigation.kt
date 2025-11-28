package com.legozia.files.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.legozia.files.viewmodel.FileManagerViewModel

@Composable
fun AppNavigation(
    viewModel: FileManagerViewModel
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "fileManager") {
        composable("fileManager") {
            FileManagerScreen(
                viewModel = viewModel,
                onNavigateToAnalyzeStorage = { navController.navigate("analyzeStorage") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("analyzeStorage") {
            AnalyzeStorageScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}