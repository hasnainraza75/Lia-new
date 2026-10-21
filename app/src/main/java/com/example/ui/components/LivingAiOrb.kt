package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.local.OrbState
import com.example.data.local.OrbStyle
import com.example.ui.theme.LocalCustomColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LivingAiOrb(
    orbState: OrbState,
    orbStyle: OrbStyle = OrbStyle.CLASSIC,
    audioAmplitude: Float = 0f, // 0.0 to 1.0 from mic or speech synthesis
    reduceMotion: Boolean = false,
    size: Dp = 220.dp,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "orbTransition")

    // Idle / ambient floating breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reduceMotion) 3000 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingPulse"
    )

    // Rotation angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.THINKING -> if (reduceMotion) 4000 else 1400
                    OrbState.LISTENING -> if (reduceMotion) 6000 else 3000
                    OrbState.SPEAKING -> if (reduceMotion) 5000 else 2400
                    else -> if (reduceMotion) 12000 else 6000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    // Wave ring expansion
    val ringExpansion by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reduceMotion) 2200 else 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringExpansion"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reduceMotion) 2200 else 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringAlpha"
    )

    // Dynamic scale driven by audio amplitude and state
    val dynamicAudioScale = remember(audioAmplitude, orbState, breathingPulse) {
        when (orbState) {
            OrbState.LISTENING -> 1.0f + (audioAmplitude * 0.28f)
            OrbState.SPEAKING -> 1.0f + (audioAmplitude * 0.32f)
            OrbState.THINKING -> 1.02f
            OrbState.ERROR -> 0.98f
            OrbState.IDLE -> breathingPulse
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .testTag("interactive_ai_orb")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = (size.toPx() / 2f) * 0.62f
            val orbRadius = baseRadius * dynamicAudioScale

            // Draw outer ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = when (orbState) {
                        OrbState.ERROR -> listOf(Color(0xFFFF3B30).copy(alpha = 0.35f), Color.Transparent)
                        OrbState.THINKING -> listOf(colors.accentCyan.copy(alpha = 0.45f), Color.Transparent)
                        OrbState.LISTENING -> listOf(colors.orbPrimary.copy(alpha = 0.50f), Color.Transparent)
                        OrbState.SPEAKING -> listOf(colors.accentGold.copy(alpha = 0.45f), colors.orbPrimary.copy(alpha = 0.3f), Color.Transparent)
                        OrbState.IDLE -> listOf(colors.orbPrimary.copy(alpha = 0.28f), Color.Transparent)
                    },
                    center = center,
                    radius = baseRadius * 1.55f
                ),
                radius = baseRadius * 1.55f,
                center = center
            )

            // Draw pulsing outer rings for active states (Listening, Speaking)
            if (orbState == OrbState.LISTENING || orbState == OrbState.SPEAKING) {
                drawCircle(
                    color = if (orbState == OrbState.LISTENING) colors.accentCyan.copy(alpha = ringAlpha)
                    else colors.accentGold.copy(alpha = ringAlpha),
                    radius = baseRadius * ringExpansion,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = colors.orbPrimary.copy(alpha = (ringAlpha * 0.8f)),
                    radius = baseRadius * (ringExpansion * 1.15f),
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Draw Metallic Gold / Violet outer orbital ring
            drawOrbitalRing(center, baseRadius * 1.18f, rotationAngle, colors.accentGold, colors.accentCyan, reduceMotion)

            // Draw Core 3D Spherical Orb
            drawCore3dOrb(center, orbRadius, orbState, orbStyle, rotationAngle, colors.orbPrimary, colors.orbSecondary, colors.accentCyan, colors.accentGold)

            // Star / energy particles inside and around orb
            if (!reduceMotion && orbStyle != OrbStyle.MINIMAL) {
                drawCosmicParticles(center, baseRadius, rotationAngle, orbState, colors.accentCyan, colors.accentGold)
            }
        }
    }
}

private fun DrawScope.drawOrbitalRing(
    center: Offset,
    radius: Float,
    angle: Float,
    goldColor: Color,
    cyanColor: Color,
    reduceMotion: Boolean
) {
    // Outer technical gold bezel ring
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                goldColor.copy(alpha = 0.7f),
                goldColor.copy(alpha = 0.15f),
                cyanColor.copy(alpha = 0.8f),
                goldColor.copy(alpha = 0.15f),
                goldColor.copy(alpha = 0.7f)
            ),
            center = center
        ),
        radius = radius,
        center = center,
        style = Stroke(width = 2.5.dp.toPx())
    )

    // Orbital satellites / tech notches
    val notchCount = 4
    for (i in 0 until notchCount) {
        val rad = Math.toRadians((angle + i * 90.0)).toFloat()
        val dotOffset = Offset(
            center.x + radius * cos(rad),
            center.y + radius * sin(rad)
        )
        drawCircle(
            color = if (i % 2 == 0) goldColor else cyanColor,
            radius = 3.5.dp.toPx(),
            center = dotOffset
        )
    }
}

private fun DrawScope.drawCore3dOrb(
    center: Offset,
    radius: Float,
    state: OrbState,
    style: OrbStyle,
    angle: Float,
    primaryColor: Color,
    secondaryColor: Color,
    cyanColor: Color,
    goldColor: Color
) {
    // 3D Spherical lighting gradient with highlight offset towards top-left
    val lightOffset = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)

    val coreColors = when (state) {
        OrbState.ERROR -> listOf(
            Color(0xFFFF8E82),
            Color(0xFFDC2626),
            Color(0xFF7F1D1D),
            Color(0xFF350B0B)
        )
        OrbState.THINKING -> listOf(
            Color(0xFFE0F7FA),
            cyanColor,
            Color(0xFF0284C7),
            Color(0xFF03254C)
        )
        OrbState.SPEAKING -> listOf(
            Color(0xFFFFF7ED),
            goldColor,
            primaryColor,
            Color(0xFF1E0A3C)
        )
        OrbState.LISTENING -> listOf(
            Color(0xFFF3E8FF),
            cyanColor,
            primaryColor,
            Color(0xFF140827)
        )
        OrbState.IDLE -> when (style) {
            OrbStyle.GALAXY -> listOf(Color(0xFFFDF4FF), primaryColor, secondaryColor, Color(0xFF0A0216))
            OrbStyle.AURORA -> listOf(Color(0xFFE0F2FE), cyanColor, Color(0xFF10B981), Color(0xFF022C22))
            OrbStyle.MINIMAL -> listOf(Color(0xFFDDD6FE), primaryColor, Color(0xFF1E1B4B))
            OrbStyle.CLASSIC -> listOf(Color(0xFFEDE9FE), primaryColor, secondaryColor, Color(0xFF0F0728))
        }
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = coreColors,
            center = lightOffset,
            radius = radius * 1.3f
        ),
        radius = radius,
        center = center
    )

    // Inner energy swirls / vortex
    val swirlCount = 3
    for (i in 0 until swirlCount) {
        val swirlAngle = angle + (i * 120f)
        val swirlRad = Math.toRadians(swirlAngle.toDouble()).toFloat()
        val swirlCenter = Offset(
            center.x + (radius * 0.25f) * cos(swirlRad),
            center.y + (radius * 0.25f) * sin(swirlRad)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    cyanColor.copy(alpha = 0.45f),
                    Color.Transparent
                ),
                center = swirlCenter,
                radius = radius * 0.55f
            ),
            radius = radius * 0.55f,
            center = swirlCenter
        )
    }

    // Top-left specular highlight (gives the glass-orb 3D sheen)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.75f), Color.Transparent),
            center = lightOffset,
            radius = radius * 0.40f
        ),
        radius = radius * 0.35f,
        center = lightOffset
    )
}

private fun DrawScope.drawCosmicParticles(
    center: Offset,
    baseRadius: Float,
    angle: Float,
    state: OrbState,
    cyan: Color,
    gold: Color
) {
    val particleCount = if (state == OrbState.THINKING) 12 else 7
    for (i in 0 until particleCount) {
        val speedFactor = (i % 3 + 1) * 0.8f
        val currentA = (angle * speedFactor + i * (360f / particleCount))
        val rad = Math.toRadians(currentA.toDouble()).toFloat()
        val dist = baseRadius * (0.85f + (i % 4) * 0.12f)
        val pOffset = Offset(
            center.x + dist * cos(rad),
            center.y + dist * sin(rad)
        )
        drawCircle(
            color = if (i % 2 == 0) cyan.copy(alpha = 0.75f) else gold.copy(alpha = 0.85f),
            radius = (1.5f + (i % 3)).dp.toPx(),
            center = pOffset
        )
    }
}
