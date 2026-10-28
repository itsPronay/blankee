package com.pronaycoding.blankee

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pronaycoding.blankee.feature.home.HomeScreenRoute
import com.pronaycoding.blankee.feature.settings.SettingsScreenRoute

/**
 * Navigation routes for the Blankee application.
 *
 * This enum defines all available destinations in the app's navigation graph.
 * Routes are used by Jetpack Compose Navigation to manage screen transitions.
 *
 * @see Navigation for the NavHost setup that uses these routes
 */
enum class Routes {
    /**
     * Home screen route - main screen showing sound controls and presets.
     * Entry point for the app.
     */
    Home,

    /**
     * Settings screen route - app preferences, theme, language, and premium features.
     * Accessible from the home screen.
     */
    Settings,
}

/**
 * Sets up the navigation graph for the Blankee application.
 *
 * This composable creates a NavHost with all app routes and their associated screen composables.
 * It manages navigation between:
 * - Home screen (default/start destination)
 * - Settings screen
 *
 * Uses Jetpack Compose Navigation (androidx.navigation.compose) for type-safe navigation.
 * The NavController is created with rememberNavController() and managed internally.
 *
 * @see Routes for available navigation destinations
 * @see HomeScreenRoute for the home screen composable
 * @see SettingsScreenRoute for the settings screen composable
 */
@Composable
fun Navigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home.name,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        /**
         * Home screen - displays sound selection, volume controls, and presets.
         */
        composable(Routes.Home.name) {
            HomeScreenRoute(
                navigateToSettings = { navController.navigate(Routes.Settings.name) },
            )
        }

        /**
         * Settings screen - app configuration including theme, language, and premium.
         */
        composable(Routes.Settings.name) {
            SettingsScreenRoute(
                onBackPressed = { navController.navigateUp() },
            )
        }
    }
}
