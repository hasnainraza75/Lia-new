package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OrbState
import com.example.data.local.OrbStyle
import com.example.ui.components.LivingAiOrb
import com.example.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val colors = LocalCustomColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "splashStars")

    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha"
    )

    // Stars seed
    val stars = remember {
        List(60) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 2.5f + 1f)
        }
    }

    LaunchedEffect(Unit) {
        // Genuine initialization delay
        delay(1800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF040209),
                        Color(0xFF140828),
                        Color(0xFF080312)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Galaxy particle field
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            for (star in stars) {
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha * (0.4f + star.first * 0.6f)),
                    radius = star.third.dp.toPx(),
                    center = Offset(star.first * w, star.second * h)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Central 3D Living AI Energy Orb
            LivingAiOrb(
                orbState = OrbState.IDLE,
                orbStyle = OrbStyle.GALAXY,
                audioAmplitude = 0.2f,
                size = 200.dp,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "NOVA AI",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color.White,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "YOUR PERSONAL AI COMPANION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.accentGold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = colors.accentCyan,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Initializing neural assistant...",
                fontSize = 12.sp,
                color = colors.textMuted
            )
        }
    }
}
