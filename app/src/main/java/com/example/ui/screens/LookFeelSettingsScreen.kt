package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OrbStyle
import com.example.data.local.ThemeMode
import com.example.ui.theme.LocalCustomColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LookFeelSettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val colors = LocalCustomColors.current
    val preferences by viewModel.preferences.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.bgUpper,
                        colors.bgMain
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Look & Feel",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Visual themes, orb styles & motion settings",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Color Themes
            SettingsSectionHeader(title = "Color Themes")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = preferences.themeMode == mode
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.preferencesRepository.updatePreferences { it.copy(themeMode = mode) }
                            },
                        color = if (isSelected) colors.surfaceHighlight else colors.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) colors.accentGold else colors.borderLavender
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Theme palette swatches
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    when (mode) {
                                        ThemeMode.DARK_PURPLE -> {
                                            ColorDot(Color(0xFF160B28))
                                            ColorDot(Color(0xFF8B5CF6))
                                            ColorDot(Color(0xFFF5C542))
                                        }
                                        ThemeMode.BLACK_GOLD -> {
                                            ColorDot(Color(0xFF060606))
                                            ColorDot(Color(0xFFD4AF37))
                                            ColorDot(Color(0xFFFFDF73))
                                        }
                                        ThemeMode.MIDNIGHT_BLUE -> {
                                            ColorDot(Color(0xFF040814))
                                            ColorDot(Color(0xFF2563EB))
                                            ColorDot(Color(0xFF38BDF8))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = mode.displayName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = when (mode) {
                                            ThemeMode.DARK_PURPLE -> "Deep violet gradients, electric cyan & warm gold"
                                            ThemeMode.BLACK_GOLD -> "Obsidian midnight with radiant champagne gold"
                                            ThemeMode.MIDNIGHT_BLUE -> "Deep navy celestial space with azure glow"
                                        },
                                        fontSize = 11.sp,
                                        color = colors.textMuted
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = colors.accentGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Orb Styles
            SettingsSectionHeader(title = "Living 3D Orb Visual Style")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OrbStyle.entries.forEach { style ->
                    val isSelected = preferences.orbStyle == style
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.preferencesRepository.updatePreferences { it.copy(orbStyle = style) }
                            },
                        color = if (isSelected) colors.surfaceHighlight else colors.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) colors.accentCyan else colors.borderLavender
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = style.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = when (style) {
                                        OrbStyle.CLASSIC -> "Multi-layered resonant energy core with gold orbital ring"
                                        OrbStyle.GALAXY -> "Cosmic particle swirl with nebula starfield"
                                        OrbStyle.AURORA -> "Electric turquoise and emerald solar flare"
                                        OrbStyle.MINIMAL -> "Clean geometric halo for minimal distractions"
                                    },
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = colors.accentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Motion & Visual Effects
            SettingsSectionHeader(title = "Motion & Visual Lighting")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Ambient Edge Glow Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ambient Screen Edge Glow", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            Text("Pulses subtle violet & cyan edge lights while ${preferences.assistantName} is active", fontSize = 11.sp, color = colors.textMuted)
                        }
                        Switch(
                            checked = preferences.ambientEdgeGlow,
                            onCheckedChange = { checked ->
                                viewModel.preferencesRepository.updatePreferences { it.copy(ambientEdgeGlow = checked) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.accentGold,
                                checkedTrackColor = colors.accentGold.copy(alpha = 0.4f)
                            )
                        )
                    }

                    HorizontalDivider(
                        color = colors.borderLavender.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Reduce Motion Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reduce Motion", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            Text("Reduces particles, disables intense pulsing, saves battery", fontSize = 11.sp, color = colors.textMuted)
                        }
                        Switch(
                            checked = preferences.reduceMotion,
                            onCheckedChange = { checked ->
                                viewModel.preferencesRepository.updatePreferences { it.copy(reduceMotion = checked) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.accentCyan,
                                checkedTrackColor = colors.accentCyan.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
            .border(0.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
    )
}
