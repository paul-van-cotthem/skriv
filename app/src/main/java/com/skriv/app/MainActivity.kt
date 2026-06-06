package com.skriv.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.skriv.app.data.prefs.UserPrefsData
import com.skriv.app.data.prefs.UserPreferences
import com.skriv.app.data.repository.FileRepository
import com.skriv.app.navigation.EditorRoute
import com.skriv.app.navigation.SkrivNavGraph
import com.skriv.app.ui.theme.SkrivTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val intentFlow = MutableStateFlow<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        intentFlow.value = intent
        setContent {
            val navController = rememberNavController()
            val userPreferences = remember { UserPreferences(applicationContext) }
            val userPrefs by userPreferences.data.collectAsStateWithLifecycle(UserPrefsData())
            val darkTheme = when (userPrefs.darkMode) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }
            val fileRepository = remember { FileRepository(applicationContext) }
            val currentIntent by intentFlow.collectAsStateWithLifecycle()
            LaunchedEffect(currentIntent) {
                currentIntent?.let { intent ->
                    val action = intent.action
                    if (action == Intent.ACTION_VIEW || action == Intent.ACTION_EDIT) {
                        intent.data?.let { uri ->
                            fileRepository.persistPermission(uri)
                            navController.navigate(EditorRoute(uri.toString()))
                            intentFlow.value = null // consume it
                        }
                    }
                }
            }
            SkrivTheme(darkTheme = darkTheme) {
                SkrivNavGraph(navController = navController)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentFlow.value = intent
    }
}
