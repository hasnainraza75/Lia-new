package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCustomColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PrivacyDataScreen(
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
                        text = "Privacy & Data",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Storage transparency & network transmission",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Where data goes Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Storage, contentDescription = null, tint = colors.accentCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("What is Stored Locally?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Chat messages and timestamps in an on-device Room SQLite database.\n" +
                               "• App settings, assistant name, custom personality prompt, and speech volume/speed in local SharedPreferences.\n" +
                               "• Your configured Gemini and Groq API keys remain strictly stored on your phone.\n" +
                               "• No personal data is sent to private third-party telemetry servers or sold.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cloud Network Transmission Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Where Do API Requests Go?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• When you send a message with Gemini enabled, text and optional photos are transmitted securely over TLS/HTTPS directly to Google AI Studio (generativelanguage.googleapis.com).\n" +
                               "• When Groq is selected, queries are sent directly to Groq Cloud (api.groq.com).\n" +
                               "• Audio recordings are never retained permanently and are discarded as soon as the voice turn finishes.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key Removal Information Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Zero-Trace Key Removal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "When you remove your API keys via the Connections screen, all provider secrets are immediately erased from the app's sandboxed storage. The app will halt cloud requests until you input a new valid key.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
