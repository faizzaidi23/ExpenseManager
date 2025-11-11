package com.example.expensecalculator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ExpenseCalculatorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
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
            onError = Color.White
        ),
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}