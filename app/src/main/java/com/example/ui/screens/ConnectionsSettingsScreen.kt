package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProviderType
import com.example.ui.theme.LocalCustomColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ConnectionsSettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalCustomColors.current
    val preferences by viewModel.preferences.collectAsState()
    val geminiStatus by viewModel.geminiStatus.collectAsState()
    val groqStatus by viewModel.groqStatus.collectAsState()
    val availableGeminiModels by viewModel.availableGeminiModels.collectAsState()
    val isFetchingModels by viewModel.isFetchingModels.collectAsState()

    var geminiKeyInput by remember(preferences.geminiApiKey) { mutableStateOf(preferences.geminiApiKey) }
    var geminiKeyVisible by remember { mutableStateOf(false) }

    var groqKeysInput by remember(preferences.groqApiKeys) { mutableStateOf(preferences.groqApiKeys) }
    var groqKeysVisible by remember { mutableStateOf(false) }

    var showClearKeysDialog by remember { mutableStateOf(false) }

    if (showClearKeysDialog) {
        AlertDialog(
            onDismissRequest = { showClearKeysDialog = false },
            title = { Text("Remove Saved API Keys?", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will erase saved Gemini and Groq API keys from local device storage.", color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.preferencesRepository.clearApiKeys()
                        geminiKeyInput = ""
                        groqKeysInput = ""
                        showClearKeysDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Remove Keys", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearKeysDialog = false }) {
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
                        text = "Connections",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Gemini & Groq API Service Integrations",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Provider Selector
            SettingsSectionHeader(title = "Default AI Providers")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Chat Provider", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(ProviderType.GEMINI, ProviderType.GROQ).forEach { p ->
                            val isSelected = preferences.chatProvider == p
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.preferencesRepository.updatePreferences { it.copy(chatProvider = p) }
                                    },
                                color = if (isSelected) colors.accentGold.copy(alpha = 0.2f) else colors.surfaceHighlight,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) colors.accentGold else Color.Transparent
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = p.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) colors.accentGold else colors.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gemini API Section
            SettingsSectionHeader(title = "Google Gemini API")
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
                        Text("Gemini Key Status", fontSize = 13.sp, color = colors.textSecondary)
                        ConnectionStatusTag(status = geminiStatus, activeColor = colors.accentCyan)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = geminiKeyInput,
                        onValueChange = { geminiKeyInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("gemini_api_key_input"),
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        visualTransformation = if (geminiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { geminiKeyVisible = !geminiKeyVisible }) {
                                Icon(
                                    imageVector = if (geminiKeyVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle visibility",
                                    tint = colors.textMuted
                                )
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Model selector header with refresh action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gemini Model",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(enabled = !isFetchingModels && geminiKeyInput.isNotBlank()) {
                                viewModel.fetchGeminiModels(geminiKeyInput.trim())
                            }
                        ) {
                            if (isFetchingModels) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = colors.accentCyan
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh models from API",
                                    tint = if (geminiKeyInput.isNotBlank()) colors.accentCyan else colors.textMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (isFetchingModels) "Fetching..." else "Refresh API Models",
                                fontSize = 11.sp,
                                color = if (geminiKeyInput.isNotBlank()) colors.accentCyan else colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scrollable dynamic model chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableGeminiModels.forEach { mod ->
                            val isSel = preferences.geminiModel == mod
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.preferencesRepository.updatePreferences { it.copy(geminiModel = mod) }
                                    },
                                color = if (isSel) colors.accentCyan.copy(alpha = 0.2f) else colors.surfaceHighlight,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) colors.accentCyan else colors.borderLavender.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSel) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = colors.accentCyan,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = mod,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) colors.accentCyan else colors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Detailed error message card when validation fails
                    if (geminiStatus.startsWith("Error:", ignoreCase = true)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = geminiStatus.removePrefix("Error:").trim(),
                                    fontSize = 11.sp,
                                    color = Color(0xFFFCA5A5),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Get key from Google AI Studio",
                            fontSize = 12.sp,
                            color = colors.accentCyan,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                                context.startActivity(intent)
                            }
                        )

                        Button(
                            onClick = {
                                val cleanKey = geminiKeyInput.trim()
                                viewModel.preferencesRepository.updatePreferences { it.copy(geminiApiKey = cleanKey) }
                                viewModel.testGeminiKey(cleanKey, preferences.geminiModel)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accentCyan),
                            modifier = Modifier.testTag("validate_gemini_button")
                        ) {
                            Text("Save & Test", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Groq API Section
            SettingsSectionHeader(title = "Groq Cloud API (Multi-Key Rotation)")
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
                        Text("Groq Key Status", fontSize = 13.sp, color = colors.textSecondary)
                        ConnectionStatusTag(status = groqStatus, activeColor = colors.accentGold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = groqKeysInput,
                        onValueChange = { groqKeysInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("groq_api_keys_input"),
                        label = { Text("Groq API Key(s)") },
                        placeholder = { Text("gsk_..., gsk_... (comma-separated)") },
                        visualTransformation = if (groqKeysVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { groqKeysVisible = !groqKeysVisible }) {
                                Icon(
                                    imageVector = if (groqKeysVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle visibility",
                                    tint = colors.textMuted
                                )
                            }
                        },
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Model selector
                    Text("Groq Chat Model", fontSize = 12.sp, color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant").forEach { mod ->
                            val isSel = preferences.groqModel == mod
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.preferencesRepository.updatePreferences { it.copy(groqModel = mod) }
                                    },
                                color = if (isSel) colors.accentGold.copy(alpha = 0.2f) else colors.surfaceHighlight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) colors.accentGold else Color.Transparent)
                            ) {
                                Text(
                                    text = if (mod.contains("70b")) "Llama 3.3 70B" else "Llama 3.1 8B",
                                    fontSize = 11.sp,
                                    color = if (isSel) colors.accentGold else colors.textMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Get key from Groq Console",
                            fontSize = 12.sp,
                            color = colors.accentGold,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.groq.com/keys"))
                                context.startActivity(intent)
                            }
                        )

                        Button(
                            onClick = {
                                viewModel.preferencesRepository.updatePreferences { it.copy(groqApiKeys = groqKeysInput.trim()) }
                                viewModel.testGroqKeys(groqKeysInput.trim(), preferences.groqModel)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                            modifier = Modifier.testTag("validate_groq_button")
                        ) {
                            Text("Save & Test", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Remove keys button
            OutlinedButton(
                onClick = { showClearKeysDialog = true },
                modifier = Modifier.fillMaxWidth().testTag("clear_api_keys_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
            ) {
                Icon(imageVector = Icons.Filled.KeyOff, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Remove All Stored API Keys")
            }
        }
    }
}

@Composable
fun ConnectionStatusTag(status: String, activeColor: Color) {
    val isReady = status.contains("Connected", ignoreCase = true) || status.contains("Ready", ignoreCase = true)
    val isError = status.contains("Error", ignoreCase = true)
    val color = when {
        isReady -> activeColor
        isError -> Color(0xFFEF4444)
        else -> Color(0xFF71717A)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = status,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
