package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = OceanCyanLight,
    secondary = OceanCyan,
    tertiary = OceanAmber,
    background = OceanDark,
    surface = OceanSurface,
    onPrimary = OceanDark,
    onSecondary = OceanDark,
    onBackground = OceanOnDark,
    onSurface = OceanOnDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = OceanCyan,
    secondary = OceanSurface,
    tertiary = OceanAmber,
    background = OceanDark,
    surface = OceanSurface,
    onPrimary = OceanDark,
    onSecondary = OceanOnDark,
    onBackground = OceanOnDark,
    onSurface = OceanOnDark
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
