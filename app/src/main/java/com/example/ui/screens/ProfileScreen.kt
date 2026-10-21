package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.LivingAiOrb
import com.example.ui.theme.LocalCustomColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val colors = LocalCustomColors.current
    val preferences by viewModel.preferences.collectAsState()
    val orbState by viewModel.orbState.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember(preferences.userDisplayName) { mutableStateOf(preferences.userDisplayName) }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Your Name", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Your Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.preferencesRepository.updatePreferences { it.copy(userDisplayName = nameInput.trim()) }
                        }
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
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
                Text(
                    text = "Profile & Identity",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User Profile Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(colors.accentGold.copy(alpha = 0.15f))
                                .border(1.5.dp, colors.accentGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = colors.accentGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = preferences.userDisplayName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Local Companion User",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    IconButton(onClick = {
                        nameInput = preferences.userDisplayName
                        showEditNameDialog = true
                    }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Name", tint = colors.accentGold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Assistant Avatar & Identity Card
            SettingsSectionHeader(title = "Your AI Companion Profile")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceHighlight)
                            .border(2.dp, colors.accentGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        LivingAiOrb(
                            orbState = orbState,
                            orbStyle = preferences.orbStyle,
                            audioAmplitude = 0f,
                            size = 90.dp,
                            onClick = {}
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = preferences.assistantName,
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "NOVA Intelligence Engine",
                        fontSize = 12.sp,
                        color = colors.accentCyan
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = colors.borderLavender.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Detail rows
                    ProfileDetailRow(label = "Personality", value = preferences.personality.displayName)
                    ProfileDetailRow(label = "Active Voice", value = "${preferences.selectedVoice.voiceName} (${preferences.voiceGender.name.lowercase().replaceFirstChar { it.uppercase() }})")
                    ProfileDetailRow(label = "Primary Language", value = preferences.language.displayName)
                    ProfileDetailRow(label = "Chat Provider", value = preferences.chatProvider.displayName)
                    ProfileDetailRow(label = "Memory Retention", value = if (preferences.memoryEnabled) "Enabled (Local)" else "Disabled")
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    val colors = LocalCustomColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = colors.textMuted)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
    }
}
