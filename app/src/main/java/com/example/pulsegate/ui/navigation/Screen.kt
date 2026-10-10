package com.example.pulsegate.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Game : Screen("game")
    object Result : Screen("result/{score}/{isNewBest}/{perfectCount}/{gatesCount}") {
        fun createRoute(score: Int, isNewBest: Boolean, perfectCount: Int, gatesCount: Int): String {
            return "result/$score/$isNewBest/$perfectCount/$gatesCount"
        }
    }
    object Settings : Screen("settings")
    object About : Screen("about")
}

sealed class BottomTab(val route: String, val label: String) {
    object Home : BottomTab("home", "Home")
    object Settings : BottomTab("settings", "Settings")
    object About : BottomTab("about", "About")
}
