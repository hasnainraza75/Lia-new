package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
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
import com.example.ui.theme.LocalCustomColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ChatsMemoryScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val colors = LocalCustomColors.current
    val preferences by viewModel.preferences.collectAsState()
    val conversations by viewModel.conversationsList.collectAsState()

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearMemoryDialog by remember { mutableStateOf(false) }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Conversation History?", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("All local conversation logs and messages will be removed.", color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = colors.textMuted)
                }
            },
            containerColor = colors.surfaceCard,
            shape = RoundedCornerShape(18.dp)
        )
    }

    if (showClearMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearMemoryDialog = false },
            title = { Text("Reset Long-Term Memory?", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will erase the assistant's long-term memory recap of your preferences and habits.", color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.preferencesRepository.clearMemory()
                        showClearMemoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold)
                ) {
                    Text("Reset Memory", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearMemoryDialog = false }) {
                    Text("Cancel", color = colors.textMuted)
                }
            },
            containerColor = colors.surfaceCard,
            shape = RoundedCornerShape(18.dp)
        )
    }

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
                        text = "Chats & Memory",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Manage saved conversations & AI recollections",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Memory Toggle Card
            SettingsSectionHeader(title = "AI Companion Memory")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Enable Adaptive Memory", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text(text = "Allows ${preferences.assistantName} to remember your tone, language, and interests across sessions", fontSize = 11.sp, color = colors.textMuted)
                        }
                        Switch(
                            checked = preferences.memoryEnabled,
                            onCheckedChange = { checked ->
                                viewModel.preferencesRepository.updatePreferences { it.copy(memoryEnabled = checked) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.accentGold,
                                checkedTrackColor = colors.accentGold.copy(alpha = 0.4f)
                            )
                        )
                    }

                    if (preferences.memoryEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = colors.borderLavender.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(text = "Active Memory Summary:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accentGold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = preferences.savedMemorySummary.ifBlank { "No long-term memories stored yet." },
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            lineHeight = 17.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surfaceHighlight, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = { showClearMemoryDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF87171))
                        ) {
                            Text("Clear Memory Summary", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Conversation History Statistics & Management
            SettingsSectionHeader(title = "Local Conversation Logs")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Saved Conversations", fontSize = 14.sp, color = colors.textPrimary)
                        Text(text = "${conversations.size} chats", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.accentCyan)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Conversations are stored strictly in an encrypted Room SQLite database on your device.",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showClearHistoryDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth().testTag("clear_history_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete All Chat History", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
