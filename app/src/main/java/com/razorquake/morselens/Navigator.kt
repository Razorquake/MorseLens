package com.razorquake.morselens

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.razorquake.morselens.ui.morse_code_translator.MorseCodeTranslator
import com.razorquake.morselens.ui.morse_code_translator.MorseCodeViewModel
import com.razorquake.morselens.ui.components.AnimatedNavigationBar
import com.razorquake.morselens.ui.components.AnimatedNavigationRail
import com.razorquake.morselens.ui.dictionary.DictionaryScreen
import com.razorquake.morselens.ui.dictionary.DictionaryViewModel
import com.razorquake.morselens.ui.flashlight.FlashDetector
import com.razorquake.morselens.ui.settings.SettingsScreen
import com.razorquake.morselens.ui.settings.SettingsViewModel
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
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
    val configuration = LocalConfiguration.current
    val windowSizeClass = calculateWindowSizeClass(
        activity = navController.context as Activity 
    )

    // Determine if we should use landscape mode navigation
    val useLandscapeNav = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact

    // Observe the current backstack entry and update selectedItem when it changes.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var selectedItem by rememberSaveable {
        mutableIntStateOf(0)
    }

    selectedItem = buttonItems
        .indexOfFirst { screen ->
            currentDestination.isRouteInHierarchy(screen::class)
        }.takeIf {
            it != -1
        }?:0
    val isBottomBarVisible by remember(
        currentDestination
    ) {
        derivedStateOf {
            buttonItems.any {
                currentDestination.isRouteInHierarchy(it::class)
            }
        }
    }
    if (useLandscapeNav) {
        // Landscape layout with navigation rail
        val navigationBarsInsets = WindowInsets.navigationBars
        val cutoutInsets = WindowInsets.displayCutout

        // Get left inset values
        val navigationBarsRight = with(LocalDensity.current) {
            navigationBarsInsets.getRight(this, LayoutDirection.Rtl)
        }
        val cutoutRight = with(LocalDensity.current) {
            cutoutInsets.getRight(this, LayoutDirection.Rtl)
        }

        // Create custom WindowInsets for left side only
        val rightInsets = remember(navigationBarsRight, cutoutRight) {
            if (navigationBarsRight > 0 && cutoutRight > 0) {
                // If both affect the left, use the larger one
                val maxLeft = maxOf(navigationBarsRight, cutoutRight)
                WindowInsets(right = maxLeft)
            } else if (navigationBarsRight > 0) {
                WindowInsets(right = navigationBarsRight)
            } else if (cutoutRight > 0) {
                WindowInsets(right = cutoutRight)
            } else {
                WindowInsets(0)
            }
        }

        // Calculate the width of the rail to use as left padding for content
        var railWidth by remember { mutableStateOf(0.dp) }

        Box(modifier = Modifier.fillMaxSize()) {

            val rightInsetDp = with(LocalDensity.current) {
                rightInsets.getRight(this, LayoutDirection.Rtl).toDp()
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
            ) {
                NavHost(
                    innerPadding = PaddingValues(
                        start = if (isBottomBarVisible) railWidth else 0.dp,
                        end = rightInsetDp
                    ),
                    navController = navController
                )
            }

            if (isBottomBarVisible) {
                val density = LocalDensity.current
                Box(
                    modifier = Modifier
                        .zIndex(1f)
                        .onGloballyPositioned { coordinates ->
                            // Store the width of the rail
                            railWidth = with(density) { coordinates.size.width.toDp() }
                        }
                ) {
                    AnimatedNavigationRail(
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
            }



            // Content area


        }
    } else {
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
                innerPadding = innerPadding,
                navController = navController
            )
        }
    }
}

fun NavDestination?.isRouteInHierarchy(route: KClass<*>): Boolean =
    this?.hierarchy?.any { it.hasRoute(route) } == true

@Composable
fun NavHost(
    innerPadding: PaddingValues,
    navController: NavHostController,
) {
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
            val state by viewModel.state.collectAsStateWithLifecycle()
            MorseCodeTranslator(
                state, viewModel::onEvent,
                innerPadding = innerPadding
            )
        }
        composable<Screen.FlashDetector> {
            FlashDetector(
                bottomPadding = innerPadding.calculateBottomPadding(),
                topPadding = innerPadding.calculateTopPadding(),
                rightPadding = innerPadding.calculateRightPadding(
                    LayoutDirection.Ltr
                ),
                leftPadding = innerPadding.calculateLeftPadding(
                    LayoutDirection.Ltr
                )
            )
        }
        composable<Screen.Dictionary> {
            val viewModel: DictionaryViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            DictionaryScreen(
                state, viewModel::onEvent,
                innerPadding = innerPadding
            )
        }
        composable<Screen.Settings> {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(viewModel)
        }
    }
}