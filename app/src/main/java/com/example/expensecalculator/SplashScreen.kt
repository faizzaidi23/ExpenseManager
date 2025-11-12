package com.example.expensecalculator

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SplashScreen composable with:
 * - Gradient purple background
 * - Glow behind icon
 * - Staggered animations (entry fade/scale)
 * - Bouncing dots with staggered delays
 * - Fade-out after 2.5s and navigate after 3s (onComplete)
 */
@Composable
fun SplashScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // controls the overall fade out (0f -> 1f)
    var isFadingOut by remember { mutableStateOf(false) }

    // start timers (2.5s fade start, 3s completion)
    LaunchedEffect(Unit) {
        delay(2500L)                 // wait 2.5s then start fade
        isFadingOut = true
        delay(500L)                  // remaining time until 3s total
        onComplete()
    }

    // animate overall alpha (fade out)
    val overallAlpha by animateFloatAsState(
        targetValue = if (isFadingOut) 0f else 1f,
        animationSpec = tween(durationMillis = 500), label = ""
    )

    // Entrance animation for content (initial appearance: scale+alpha)
    val entranceScale = remember { Animatable(0.95f) }
    val entranceAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        // small delay to make entrance feel staggered (like your animate-in delays)
        launch {
            delay(80L)
            entranceAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
        }
        launch {
            delay(40L)
            entranceScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = OvershootInterpolatorEasing())
            )
        }
    }

    // Compose layout
    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(overallAlpha)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Purple900, Purple700, Purple500
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(entranceScale.value)
                .alpha(entranceAlpha.value)
                .padding(24.dp)
        ) {
            // Icon + glow
            Box(contentAlignment = Alignment.Center) {
                // glow behind icon (radial)
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .offset(y = (-8).dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x66FFFFFF), Color(0x00FFFFFF)),
                                center = androidx.compose.ui.geometry.Offset.Unspecified,
                                radius = 120f
                            ),
                            shape = CircleShape
                        )
                )

                // Icon (replace AppIcon() with your actual icon)
                AppIcon(size = 160.dp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App name & subtitle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ExpenseFlow",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFAFAFF)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Track • Manage • Analyze",
                    fontSize = 13.sp,
                    color = Color(0xFFECF0FF)
                )
            }

            // Loading dots
            Spacer(modifier = Modifier.height(36.dp))

            LoadingDots()
        }

        // Version text at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            Text(text = "Version 1.0.0", fontSize = 12.sp, color = Color(0xFFECF0FF))
        }
    }
}

/** Simple bouncing loading dots with staggered start offsets */
@Composable
private fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Each dot has same animation but different StartOffset (stagger)
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(0)
        ), label = ""
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(150)
        ), label = ""
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(300)
        ), label = ""
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Dot(offsetY = dot1)
        Dot(offsetY = dot2)
        Dot(offsetY = dot3)
    }
}

@Composable
private fun Dot(offsetY: Float) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .offset(y = offsetY.dp)
            .clip(CircleShape)
            .background(Color(0xFFFAFAFF))
    )
}

/** Placeholder AppIcon - replace implementation with your actual image or vector */
@Composable
fun AppIcon(size: Dp, modifier: Modifier = Modifier) {
    // Use the launcher icon directly
    val painter: Painter? = painterResource(id = R.mipmap.ic_launcher)

    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = "App Icon",
            modifier = modifier.size(size),
            contentScale = ContentScale.Fit
        )
    } else {
        // fallback: simple circle with text (quick placeholder)
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(0xFF6B46C1)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "EF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 48.sp)
        }
    }
}

/** Utility: Overshoot easing created through Interpolator-like behavior */
private fun OvershootInterpolatorEasing() = CubicBezierEasing(0.2f, 1.5f, 0.5f, 1.0f)

/** --- Colors (use in your Theme) --- */
private val Purple900 = Color(0xFF3B0D72)
private val Purple700 = Color(0xFF6B21A8)
private val Purple500 = Color(0xFF9B59FF)
