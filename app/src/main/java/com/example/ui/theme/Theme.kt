package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFFFCC00), // Iconic Locket Canary Yellow
    secondary = Color(0xFFFFA726), // Smooth orange warning variant
    tertiary = Color(0xFF66BB6A), // Fresh green indicator
    background = Color(0xFF0F0F0F), // Very deep cinema black
    surface = Color(0xFF1E1E1E), // Premium dark grey cards
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
  )

private val LightColorScheme = DarkColorScheme // Always maintain premium dark look for viewfinder elegance

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark mode for a cinematic feel
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve our customized Locket identity
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
