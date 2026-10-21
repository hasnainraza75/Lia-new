package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCustomColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PrivacyAboutScreen(
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
                        text = "Privacy & About",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Policy, terms & application information",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // App Version Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "NOVA AI Assistant", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(text = "Personal AI Companion • Production Build", fontSize = 12.sp, color = colors.textMuted)
                    }
                    Text(
                        text = "v1.0.0",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accentGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy Policy
            SettingsSectionHeader(title = "Privacy Policy")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Information Collection & Usage\n" +
                               "NOVA AI processes audio from your device's microphone exclusively when you tap or hold the voice session orb. Audio is evaluated strictly for transcription and speech-to-text response generation and is immediately purged.\n\n" +
                               "2. Contacts & Device Actions\n" +
                               "When you request phone calls or messages (e.g. \"Call Mummy\"), contacts permission is used strictly on-device to match names with numbers. Your contact list is never uploaded wholesale to remote servers.\n\n" +
                               "3. External AI Service Providers\n" +
                               "Requests made to Google Gemini or Groq Cloud are governed by their respective privacy terms. No proprietary telemetry tracking scripts or ad frameworks are embedded.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Terms of Service
            SettingsSectionHeader(title = "Terms of Service")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "• AI-Generated Content: Responses are generated by large language models for informational and entertainment purposes. You should verify crucial technical, financial, or medical guidance.\n\n" +
                               "• Phone Calls and Messages: Calling and messaging actions require user review and explicit confirmation before dialing or dispatching drafts.\n\n" +
                               "• Third-Party Availability: Uptime and rate limits of Gemini and Groq are subject to the user's active API quotas.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
