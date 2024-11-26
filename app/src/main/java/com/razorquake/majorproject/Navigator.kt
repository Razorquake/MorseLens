package com.razorquake.majorproject

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.razorquake.majorproject.home.HomeScreen
import com.razorquake.majorproject.morse_code_translator.MorseCodeTranslator
import com.razorquake.majorproject.morse_code_translator.MorseCodeViewModel

@Composable
fun Navigator(){
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        val bottomPadding = it.calculateBottomPadding()
        val topPadding = it.calculateTopPadding()
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = Modifier.padding(bottom = bottomPadding, top = topPadding)
        ){
            composable<Screen.Home> {
                HomeScreen(
                    onMorseCodeTranslator = { navController.navigate(Screen.MorseCodeTranslator) },
                    onFlashDetector = { navController.navigate(Screen.FlashDetector) }
                )
            }
            composable<Screen.MorseCodeTranslator> {
                val viewModel: MorseCodeViewModel = viewModel()
                val state = viewModel.state.collectAsStateWithLifecycle().value
                MorseCodeTranslator(state, viewModel::onEvent)
            }
            composable<Screen.FlashDetector> {
                FlashDetector()
            }
        }

    }
}