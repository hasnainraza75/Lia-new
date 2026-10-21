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
import androidx.compose.material.icons.filled.*
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
import com.example.data.local.*
import com.example.ui.theme.LocalCustomColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun VoiceLanguageSettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val colors = LocalCustomColors.current
    val preferences by viewModel.preferences.collectAsState()

    var showNameDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(preferences.assistantName) }

    // Dialog for changing assistant name
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = {
                Text("Change Assistant Name", color = colors.textPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Choose a name for your personal AI companion:", fontSize = 13.sp, color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        modifier = Modifier.fillMaxWidth().testTag("edit_assistant_name_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            viewModel.preferencesRepository.updatePreferences {
                                it.copy(assistantName = editedName.trim())
                            }
                        }
                        showNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
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
                        text = "Voice & Language",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Natural human speech without Google TTS",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Assistant Name Tile
            SettingsSectionHeader(title = "Assistant Identity")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("setting_change_name")
                    .clickable {
                        editedName = preferences.assistantName
                        showNameDialog = true
                    },
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Assistant Name", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                        Text(text = "Currently set to \"${preferences.assistantName}\"", fontSize = 12.sp, color = colors.accentGold)
                    }
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit", tint = colors.accentGold, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Personality Options
            SettingsSectionHeader(title = "Personality & Demeanor")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Personality.entries.forEach { p ->
                    val isSelected = preferences.personality == p
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.preferencesRepository.updatePreferences { it.copy(personality = p) }
                            },
                        color = if (isSelected) colors.surfaceHighlight else colors.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) colors.accentGold else colors.borderLavender
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.preferencesRepository.updatePreferences { it.copy(personality = p) }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.accentGold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = p.displayName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                Text(text = p.description, fontSize = 11.sp, color = colors.textMuted)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Language Selection
            SettingsSectionHeader(title = "Language Preferences")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageOption.entries.forEach { lang ->
                    val isSelected = preferences.language == lang
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.preferencesRepository.updatePreferences { it.copy(language = lang) }
                            },
                        color = if (isSelected) colors.surfaceHighlight else colors.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) colors.accentCyan else colors.borderLavender
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.preferencesRepository.updatePreferences { it.copy(language = lang) }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.accentCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lang.displayName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Natural Voices Selection (NO Google TTS)
            SettingsSectionHeader(title = "Natural Voice Selection")
            Text(
                text = "Powered by independent acoustic neural modeling. Independent of Google Speech Services.",
                fontSize = 11.sp,
                color = colors.accentGold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Voice Gender Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(VoiceGender.FEMALE, VoiceGender.MALE).forEach { gender ->
                    val isSelected = preferences.voiceGender == gender
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.preferencesRepository.updatePreferences { prefs ->
                                    val newVoice = VoiceOption.entries.first { it.gender == gender }
                                    prefs.copy(voiceGender = gender, selectedVoice = newVoice)
                                }
                            },
                        color = if (isSelected) colors.accentGold.copy(alpha = 0.2f) else colors.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) colors.accentGold else colors.borderLavender
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (gender == VoiceGender.FEMALE) "Female Voices" else "Male Voices",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) colors.accentGold else colors.textMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Voice Items for selected gender
            val availableVoices = remember(preferences.voiceGender) {
                VoiceOption.entries.filter { it.gender == preferences.voiceGender }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableVoices.forEach { voice ->
                    val isSelected = preferences.selectedVoice == voice
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.preferencesRepository.updatePreferences { it.copy(selectedVoice = voice) }
                            },
                        color = if (isSelected) colors.surfaceHighlight else colors.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) colors.accentGold else colors.borderLavender
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.preferencesRepository.updatePreferences { it.copy(selectedVoice = voice) }
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = colors.accentGold)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = voice.voiceName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = voice.style,
                                            fontSize = 10.sp,
                                            color = colors.accentCyan,
                                            modifier = Modifier
                                                .background(colors.accentCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = voice.description, fontSize = 11.sp, color = colors.textMuted)
                                }
                            }

                            // Preview Voice Button
                            IconButton(
                                onClick = { viewModel.previewVoice(voice) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.accentGold.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Preview Voice",
                                    tint = colors.accentGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Speech Speed & Volume Sliders
            SettingsSectionHeader(title = "Speech Prosody & Acoustics")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Speaking Speed", fontSize = 13.sp, color = colors.textPrimary)
                        Text(text = "${"%.2f".format(preferences.speechSpeed)}x", fontSize = 13.sp, color = colors.accentGold)
                    }
                    Slider(
                        value = preferences.speechSpeed,
                        onValueChange = { spd ->
                            viewModel.preferencesRepository.updatePreferences { it.copy(speechSpeed = spd) }
                        },
                        valueRange = 0.75f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accentGold,
                            activeTrackColor = colors.accentGold
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Voice Volume", fontSize = 13.sp, color = colors.textPrimary)
                        Text(text = "${(preferences.speechVolume * 100).toInt()}%", fontSize = 13.sp, color = colors.accentCyan)
                    }
                    Slider(
                        value = preferences.speechVolume,
                        onValueChange = { vol ->
                            viewModel.preferencesRepository.updatePreferences { it.copy(speechVolume = vol) }
                        },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accentCyan,
                            activeTrackColor = colors.accentCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Listening Mode
            SettingsSectionHeader(title = "Voice Session Activation")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ListeningMode.entries.forEach { mode ->
                    val isSelected = preferences.listeningMode == mode
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.preferencesRepository.updatePreferences { it.copy(listeningMode = mode) }
                            },
                        color = if (isSelected) colors.surfaceHighlight else colors.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) colors.accentGold else colors.borderLavender
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.preferencesRepository.updatePreferences { it.copy(listeningMode = mode) }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.accentGold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = mode.displayName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    val colors = LocalCustomColors.current
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = colors.accentGold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}
