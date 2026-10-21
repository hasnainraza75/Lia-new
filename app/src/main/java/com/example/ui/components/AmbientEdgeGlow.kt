package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.local.OrbState
import com.example.ui.theme.LocalCustomColors

@Composable
fun AmbientEdgeGlow(
    orbState: OrbState,
    enabled: Boolean = true,
    reduceMotion: Boolean = false
) {
    if (!enabled) return

    val colors = LocalCustomColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "edgeGlowTransition")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reduceMotion) 2400 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "edgeGlowAlpha"
    )

    val isActive = orbState == OrbState.LISTENING || orbState == OrbState.THINKING || orbState == OrbState.SPEAKING

    if (!isActive) return

    val glowColor = when (orbState) {
        OrbState.LISTENING -> colors.orbPrimary
        OrbState.THINKING -> colors.accentCyan
        OrbState.SPEAKING -> colors.accentGold
        OrbState.ERROR -> Color(0xFFEF4444)
        OrbState.IDLE -> Color.Transparent
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val edgeThickness = 28.dp.toPx()

        // Top edge glow
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(glowColor.copy(alpha = pulseAlpha * 0.45f), Color.Transparent),
                startY = 0f,
                endY = edgeThickness
            )
        )

        // Bottom edge glow
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, glowColor.copy(alpha = pulseAlpha * 0.40f)),
                startY = h - edgeThickness,
                endY = h
            )
        )

        // Left edge glow
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(glowColor.copy(alpha = pulseAlpha * 0.25f), Color.Transparent),
                startX = 0f,
                endX = edgeThickness * 0.8f
            )
        )

        // Right edge glow
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, glowColor.copy(alpha = pulseAlpha * 0.25f)),
                startX = w - edgeThickness * 0.8f,
                endX = w
            )
        )
    }
}
