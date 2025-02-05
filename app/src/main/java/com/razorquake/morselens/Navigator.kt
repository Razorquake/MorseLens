package com.razorquake.morselens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.razorquake.morselens.home.HomeScreen
import com.razorquake.morselens.morse_code_translator.MorseCodeTranslator
import com.razorquake.morselens.morse_code_translator.MorseCodeViewModel
import com.razorquake.morselens.morse_code_translator.dictionary.DictionaryScreen
import com.razorquake.morselens.morse_code_translator.dictionary.DictionaryViewModel

@Composable
fun Navigator(modifier: Modifier) {
    val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = modifier,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )+ fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )+ fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )+ fadeIn(animationSpec = tween(700))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )+ fadeOut(animationSpec = tween(300))
            }
        ){
            composable<Screen.Home> {
                HomeScreen(
                    onMorseCodeTranslator = { navController.navigate(Screen.MorseCodeTranslator) },
                    onFlashDetector = { navController.navigate(Screen.FlashDetector) },
                    onDictionary = { navController.navigate(Screen.Dictionary) }
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
            composable<Screen.Dictionary> {
                val viewModel: DictionaryViewModel = viewModel()
                val state = viewModel.state.collectAsState().value
                DictionaryScreen(state, viewModel::onEvent)
            }
        }

}