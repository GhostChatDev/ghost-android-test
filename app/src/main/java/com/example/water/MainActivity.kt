package com.example.water

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private val LightColors = lightColorScheme(
    primary = Color(0xFF006A83), secondary = Color(0xFF416777),
    background = Color(0xFFF6FAFC), surface = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB8EAFF), surfaceVariant = Color(0xFFE0EFF4),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF65D5F7), secondary = Color(0xFFA9CBDA),
    background = Color(0xFF101C21), surface = Color(0xFF18262C),
    primaryContainer = Color(0xFF004E61), surfaceVariant = Color(0xFF31454D),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WaterApp() }
    }
}

@Composable
private fun WaterApp(home: HomeViewModel = viewModel(), settings: SettingsViewModel = viewModel()) {
    val snapshot by home.snapshot.collectAsStateWithLifecycle()
    val homeError by home.error.collectAsStateWithLifecycle()
    val settingsError by settings.error.collectAsStateWithLifecycle()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val dark = when (snapshot.theme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    BackHandler(enabled = showSettings) { showSettings = false }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (showSettings) {
                SettingsScreen(
                    snapshot = snapshot,
                    error = settingsError,
                    onBack = { showSettings = false },
                    onSaveGoal = { goal -> settings.saveGoal(goal) { showSettings = false } },
                    onTheme = settings::setTheme,
                )
            } else {
                HomeScreen(
                    snapshot = snapshot,
                    error = homeError,
                    onAdd = home::add,
                    onUndo = home::undo,
                    onSettings = { showSettings = true },
                )
            }
        }
    }
}
