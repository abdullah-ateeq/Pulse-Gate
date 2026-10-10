package com.example.pulsegate.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.pulsegate.ui.screens.AboutScreen
import com.example.pulsegate.ui.screens.GameScreen
import com.example.pulsegate.ui.screens.HomeScreen
import com.example.pulsegate.ui.screens.ResultScreen
import com.example.pulsegate.ui.screens.SettingsScreen
import com.example.pulsegate.ui.screens.SplashScreen
import com.example.pulsegate.ui.theme.NeonAqua
import com.example.pulsegate.ui.theme.NeonCyan
import com.example.pulsegate.viewmodel.GameViewModel
import com.example.pulsegate.viewmodel.SettingsViewModel

@Composable
fun PulseGateNavigation(
    navController: NavHostController,
    gameViewModel: GameViewModel,
    settingsViewModel: SettingsViewModel,
    isLightMode: Boolean,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Settings.route,
        Screen.About.route
    )

    val settings by settingsViewModel.userSettings.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                PulseGateBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Splash Screen
            composable(
                route = Screen.Splash.route,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) + scaleOut(targetScale = 1.05f, animationSpec = tween(300)) }
            ) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // 2. Home Screen
            composable(
                route = Screen.Home.route,
                enterTransition = { fadeIn(tween(250)) },
                exitTransition = { fadeOut(tween(200)) + scaleOut(targetScale = 1.1f, animationSpec = tween(250)) }
            ) {
                HomeScreen(
                    bestScore = settings.bestScore,
                    onPlayClick = {
                        navController.navigate(Screen.Game.route)
                    },
                    isLightMode = isLightMode
                )
            }

            // 3. Game Screen (Immersive gameplay)
            composable(
                route = Screen.Game.route,
                enterTransition = { scaleIn(initialScale = 0.92f, animationSpec = tween(300)) + fadeIn(tween(250)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                GameScreen(
                    viewModel = gameViewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToResult = { score, isNewBest, perfectCount, gatesCount ->
                        navController.navigate(
                            Screen.Result.createRoute(score, isNewBest, perfectCount, gatesCount)
                        ) {
                            popUpTo(Screen.Game.route) { inclusive = true }
                        }
                    },
                    isLightMode = isLightMode
                )
            }

            // 4. Result Screen
            composable(
                route = Screen.Result.route,
                arguments = listOf(
                    navArgument("score") { type = NavType.IntType },
                    navArgument("isNewBest") { type = NavType.BoolType },
                    navArgument("perfectCount") { type = NavType.IntType },
                    navArgument("gatesCount") { type = NavType.IntType }
                ),
                enterTransition = { fadeIn(tween(300)) + scaleIn(initialScale = 0.9f, animationSpec = tween(300)) },
                exitTransition = { fadeOut(tween(200)) }
            ) { backStackEntry ->
                val score = backStackEntry.arguments?.getInt("score") ?: 0
                val isNewBest = backStackEntry.arguments?.getBoolean("isNewBest") ?: false
                val perfectCount = backStackEntry.arguments?.getInt("perfectCount") ?: 0
                val gatesCount = backStackEntry.arguments?.getInt("gatesCount") ?: 0

                ResultScreen(
                    score = score,
                    isNewBest = isNewBest,
                    perfectCount = perfectCount,
                    gatesCount = gatesCount,
                    onPlayAgainClick = {
                        navController.navigate(Screen.Game.route) {
                            popUpTo(Screen.Result.route) { inclusive = true }
                        }
                    },
                    onHomeClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    isLightMode = isLightMode
                )
            }

            // 5. Settings Screen
            composable(
                route = Screen.Settings.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    isLightMode = isLightMode
                )
            }

            // 6. About Screen
            composable(
                route = Screen.About.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                AboutScreen(
                    isLightMode = isLightMode
                )
            }
        }
    }
}

@Composable
private fun PulseGateBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .border(1.dp, NeonCyan.copy(alpha = 0.2f), shape)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(64.dp)
        ) {
            NavigationBarItem(
                selected = currentRoute == Screen.Home.route,
                onClick = { onNavigate(Screen.Home.route) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "Home",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonCyan,
                    selectedTextColor = NeonCyan,
                    indicatorColor = NeonCyan.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            NavigationBarItem(
                selected = currentRoute == Screen.Settings.route,
                onClick = { onNavigate(Screen.Settings.route) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonCyan,
                    selectedTextColor = NeonCyan,
                    indicatorColor = NeonCyan.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            NavigationBarItem(
                selected = currentRoute == Screen.About.route,
                onClick = { onNavigate(Screen.About.route) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonCyan,
                    selectedTextColor = NeonCyan,
                    indicatorColor = NeonCyan.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
