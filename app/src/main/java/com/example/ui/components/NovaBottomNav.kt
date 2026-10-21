package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OrbState
import com.example.ui.theme.LocalCustomColors

enum class MainNavTab {
    HOME,
    CHAT,
    HISTORY,
    SETTINGS
}

@Composable
fun NovaBottomNav(
    currentTab: MainNavTab,
    orbState: OrbState,
    onTabSelected: (MainNavTab) -> Unit,
    onCentralOrbClick: () -> Unit
) {
    val colors = LocalCustomColors.current

    // Pulsing glow for central orb in nav bar when active
    val infiniteTransition = rememberInfiniteTransition(label = "navOrbGlow")
    val orbGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "navOrbGlowPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Pill shaped navigation bar background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.surfaceHighlight.copy(alpha = 0.95f),
                            colors.surfaceCard.copy(alpha = 0.98f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.borderLavender.copy(alpha = 0.8f),
                            colors.borderLavender.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(34.dp)
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Home Tab
            NavTabItem(
                label = "Home",
                selected = currentTab == MainNavTab.HOME,
                iconSelected = Icons.Filled.Home,
                iconUnselected = Icons.Outlined.Home,
                tag = "nav_home",
                onClick = { onTabSelected(MainNavTab.HOME) }
            )

            // Chat Tab
            NavTabItem(
                label = "Chat",
                selected = currentTab == MainNavTab.CHAT,
                iconSelected = Icons.Filled.ChatBubble,
                iconUnselected = Icons.Outlined.ChatBubbleOutline,
                tag = "nav_chat",
                onClick = { onTabSelected(MainNavTab.CHAT) }
            )

            // Placeholder space for Central Orb
            Spacer(modifier = Modifier.width(64.dp))

            // History Tab
            NavTabItem(
                label = "History",
                selected = currentTab == MainNavTab.HISTORY,
                iconSelected = Icons.Filled.History,
                iconUnselected = Icons.Outlined.History,
                tag = "nav_history",
                onClick = { onTabSelected(MainNavTab.HISTORY) }
            )

            // Settings Tab
            NavTabItem(
                label = "Settings",
                selected = currentTab == MainNavTab.SETTINGS,
                iconSelected = Icons.Filled.Settings,
                iconUnselected = Icons.Outlined.Settings,
                tag = "nav_settings",
                onClick = { onTabSelected(MainNavTab.SETTINGS) }
            )
        }

        // Central Animated Interactive AI Orb Control (raised above bar)
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(62.dp)
                .testTag("nav_central_orb_button")
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = colors.orbPrimary,
                    spotColor = colors.accentCyan
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = when (orbState) {
                            OrbState.ERROR -> listOf(Color(0xFFFF3B30), Color(0xFF7F1D1D))
                            OrbState.LISTENING -> listOf(colors.accentCyan, colors.orbPrimary)
                            OrbState.SPEAKING -> listOf(colors.accentGold, colors.orbPrimary)
                            OrbState.THINKING -> listOf(colors.accentCyan, colors.orbSecondary)
                            OrbState.IDLE -> listOf(colors.orbPrimary, Color(0xFF190B34))
                        }
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            colors.accentGold,
                            colors.accentCyan,
                            colors.orbPrimary,
                            colors.accentGold
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 32.dp),
                    onClick = onCentralOrbClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (orbState) {
                    OrbState.LISTENING -> Icons.Filled.GraphicEq
                    OrbState.SPEAKING -> Icons.Filled.VolumeUp
                    OrbState.THINKING -> Icons.Filled.AutoAwesome
                    OrbState.ERROR -> Icons.Filled.Warning
                    OrbState.IDLE -> Icons.Filled.Mic
                },
                contentDescription = "Voice Assistant Orb",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    selected: Boolean,
    iconSelected: ImageVector,
    iconUnselected: ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current

    Column(
        modifier = Modifier
            .width(56.dp)
            .fillMaxHeight()
            .testTag(tag)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) iconSelected else iconUnselected,
            contentDescription = label,
            tint = if (selected) colors.accentGold else colors.textMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) colors.accentGold else colors.textMuted
        )
    }
}
