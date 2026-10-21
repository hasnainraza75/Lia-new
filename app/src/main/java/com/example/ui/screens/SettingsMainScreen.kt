package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCustomColors
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

data class SettingsNavEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val targetScreen: Screen,
    val tag: String
)

@Composable
fun SettingsMainScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val colors = LocalCustomColors.current
    val preferences by viewModel.preferences.collectAsState()

    val settingsList = listOf(
        SettingsNavEntry("Voice & Language", "Natural voices, personality & language", Icons.Filled.RecordVoiceOver, Screen.SettingsVoiceLanguage, "settings_item_voice"),
        SettingsNavEntry("Connections", "Gemini & Groq API key setup and validation", Icons.Filled.CloudQueue, Screen.SettingsConnections, "settings_item_connections"),
        SettingsNavEntry("Look & Feel", "Themes, 3D orb styles & motion settings", Icons.Filled.Palette, Screen.SettingsLookFeel, "settings_item_look_feel"),
        SettingsNavEntry("Quick Actions", "Direct shortcuts for phone & AI tools", Icons.Filled.FlashOn, Screen.SettingsQuickActions, "settings_item_quick_actions"),
        SettingsNavEntry("Phone Control", "Microphone, Contacts & Calling permissions", Icons.Filled.PhoneAndroid, Screen.SettingsPhoneControl, "settings_item_phone_control"),
        SettingsNavEntry("Chats & Memory", "Conversation persistence & memory management", Icons.Filled.Psychology, Screen.SettingsChatsMemory, "settings_item_chats_memory"),
        SettingsNavEntry("Privacy & Data", "Local storage breakdown & key security", Icons.Filled.Security, Screen.SettingsPrivacyData, "settings_item_privacy_data"),
        SettingsNavEntry("Privacy & About", "Privacy policy, Terms of Service & version", Icons.Filled.Info, Screen.SettingsPrivacyAbout, "settings_item_privacy_about"),
        SettingsNavEntry("Profile", "User display name & assistant profile", Icons.Filled.Person, Screen.SettingsProfile, "settings_item_profile")
    )

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
                        text = "Settings",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Customize ${preferences.assistantName} and device behaviors",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Items List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(settingsList) { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .testTag(item.tag)
                            .clickable { viewModel.navigateTo(item.targetScreen) },
                        color = colors.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.accentGold.copy(alpha = 0.15f))
                                    .border(1.dp, colors.accentGold.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = colors.accentGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.subtitle,
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
