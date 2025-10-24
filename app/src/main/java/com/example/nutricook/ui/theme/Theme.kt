package com.example.nutricook.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NutriCookLight = lightColorScheme(
    primary = Teal,
    onPrimary = SurfaceWhite,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealDark,

    secondary = BlueAccent,
    tertiary = PeachAccent,

    background = Bg,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    onSurfaceVariant = OnSurfaceVar,

    error = ErrorRed,
    outline = OutlineGrey
)

@Composable
fun NutriCookTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NutriCookLight,
        typography = NutriCookTypography,
        shapes = NutriCookShapes,
        content = content
    )
}
