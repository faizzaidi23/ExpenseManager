package com.example.expensecalculator.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

fun Modifier.cardShadow(
    blur: Int = 30,
    offsetY: Int = 4
): Modifier {
    return this.shadow(
        elevation = 8.dp,
        spotColor = PrimaryBlue.copy(alpha = 0.08f),
        ambientColor = PrimaryBlue.copy(alpha = 0.08f)
    )
}

