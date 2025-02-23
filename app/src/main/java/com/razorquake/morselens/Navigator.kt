package com.razorquake.morselens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.razorquake.morselens.morse_code_translator.MorseCodeTranslator
import com.razorquake.morselens.morse_code_translator.MorseCodeViewModel
import com.razorquake.morselens.morse_code_translator.components.AnimatedNavigationBar
import com.razorquake.morselens.morse_code_translator.dictionary.DictionaryScreen
import com.razorquake.morselens.morse_code_translator.dictionary.DictionaryViewModel
import com.razorquake.morselens.settings.SettingsScreen
import com.razorquake.morselens.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigator() {
    val buttonItems = remember {
        listOf(
            Screen.MorseCodeTranslator,
            Screen.FlashDetector,
            Screen.Dictionary
        )
    }
    val navController = rememberNavController()

    // Observe the current backstack entry and update selectedItem when it changes.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var selectedItem by rememberSaveable {
        mutableIntStateOf(0)
    }

    selectedItem = when {
        currentDestination?.hierarchy?.any { it.hasRoute(Screen.MorseCodeTranslator::class) } == true -> 0
        currentDestination?.hierarchy?.any { it.hasRoute(Screen.FlashDetector::class) } == true -> 1
        currentDestination?.hierarchy?.any { it.hasRoute(Screen.Dictionary::class) } == true -> 2
        else -> 0
    }
    val isBottomBarVisible = currentDestination?.hierarchy?.any {
        it.hasRoute(Screen.MorseCodeTranslator::class) ||
                it.hasRoute(Screen.FlashDetector::class) ||
                it.hasRoute(Screen.Dictionary::class)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                actions = {
                    if (isBottomBarVisible == true)
                    IconButton(
                        onClick = { navController.navigate(Screen.Settings) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings Icon"
                        )
                    }
                },
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            if (isBottomBarVisible == true)
            AnimatedNavigationBar(
                buttons = buttonItems,
                selectedItem = selectedItem,
                onItemClick = { index ->
                    selectedItem = index
                    navController.navigate(buttonItems[index]) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.MorseCodeTranslator,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(700))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable<Screen.MorseCodeTranslator> {
                val viewModel: MorseCodeViewModel = hiltViewModel()
                val state = viewModel.state.collectAsStateWithLifecycle().value
                MorseCodeTranslator(
                    state, viewModel::onEvent,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    topPadding = innerPadding.calculateTopPadding()
                )
            }
            composable<Screen.FlashDetector> {
                FlashDetector(
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    topPadding = innerPadding.calculateTopPadding()
                )
            }
            composable<Screen.Dictionary> {
                val viewModel: DictionaryViewModel = hiltViewModel()
                val state = viewModel.state.collectAsState().value
                DictionaryScreen(
                    state, viewModel::onEvent,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    topPadding = innerPadding.calculateTopPadding()
                )
            }
            composable<Screen.Settings> {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(viewModel)
            }
        }
    }
}