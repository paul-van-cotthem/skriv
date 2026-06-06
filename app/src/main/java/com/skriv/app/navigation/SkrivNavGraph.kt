package com.skriv.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.skriv.app.ui.editor.EditorScreen
import com.skriv.app.ui.recents.RecentsScreen
import com.skriv.app.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
object RecentsRoute

@Serializable
data class EditorRoute(val uriString: String?)  // null = new document

@Serializable
object SettingsRoute

@Composable
fun SkrivNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = RecentsRoute,
        enterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.95f) },
        exitTransition = { fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.95f) },
        popEnterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.95f) },
        popExitTransition = { fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.95f) }
    ) {
        composable<RecentsRoute> {
            RecentsScreen(navController = navController)
        }
        composable<EditorRoute>(
            enterTransition = { slideInHorizontally(tween(300)) { it } },
            exitTransition = { slideOutHorizontally(tween(300)) { -it } },
            popEnterTransition = { slideInHorizontally(tween(300)) { -it } },
            popExitTransition = { slideOutHorizontally(tween(300)) { it } }
        ) { backStackEntry ->
            EditorScreen(navController = navController, navBackStackEntry = backStackEntry)
        }
        composable<SettingsRoute>(
            enterTransition = { slideInHorizontally(tween(300)) { it } },
            exitTransition = { slideOutHorizontally(tween(300)) { -it } },
            popEnterTransition = { slideInHorizontally(tween(300)) { -it } },
            popExitTransition = { slideOutHorizontally(tween(300)) { it } }
        ) {
            SettingsScreen(navController = navController)
        }
    }
}
