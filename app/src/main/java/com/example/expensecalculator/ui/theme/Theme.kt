package com.example.expensecalculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = PrimaryBlueDark,
    tertiary = PrimaryBlueLight,
    background = ScreenBackground,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    error = ErrorColor,
    onError = Color.White,
    outline = BorderGrey
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryBlue,
    secondary = DarkPrimaryBlueDark,
    tertiary = DarkPrimaryBlueLight,
    background = DarkScreenBackground,
    surface = DarkCardBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DarkPrimaryText,
    onSurface = DarkPrimaryText,
    error = DarkErrorColor,
    onError = Color.White,
    outline = DarkBorderGrey
)

@Composable
fun ExpenseCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}