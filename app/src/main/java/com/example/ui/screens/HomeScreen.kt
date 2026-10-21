package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.local.OrbState
import com.example.ui.components.AmbientEdgeGlow
import com.example.ui.components.LivingAiOrb
import com.example.ui.theme.LocalCustomColors
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToQuickActions: () -> Unit,
    onNavigateToConnections: () -> Unit
) {
    val colors = LocalCustomColors.current
    val preferences by viewModel.preferences.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val liveTranscript by viewModel.liveVoiceTranscript.collectAsState()
    val currentNotice by viewModel.currentNotice.collectAsState()
    val geminiStatus by viewModel.geminiStatus.collectAsState()
    val groqStatus by viewModel.groqStatus.collectAsState()

    // Real device local time & date
    val currentTimeStr = remember {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date())
    }
    val currentDateStr = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        sdf.format(Date())
    }

    // Dynamic greeting based on hour
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Welcome Back"
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.startVoiceListening()
    }

    val onVoiceActionClick = {
        if (orbState == OrbState.LISTENING) {
            viewModel.stopListeningAndProcess()
        } else if (orbState == OrbState.SPEAKING) {
            viewModel.voiceEngine.stop()
        } else {
            if (!viewModel.audioRecorder.hasMicrophonePermission()) {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                viewModel.startVoiceListening()
            }
        }
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
    ) {
        // Ambient Edge Lighting
        AmbientEdgeGlow(
            orbState = orbState,
            enabled = preferences.ambientEdgeGlow,
            reduceMotion = preferences.reduceMotion
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 84.dp) // Leave room for bottom nav
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Time, Date & Connection Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentDateStr.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accentGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currentTimeStr,
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                }

                // AI Service Provider status badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable { onNavigateToConnections() }
                ) {
                    ConnectionBadge(
                        label = "Gemini",
                        status = geminiStatus,
                        activeColor = colors.accentCyan
                    )
                    ConnectionBadge(
                        label = "Groq",
                        status = groqStatus,
                        activeColor = colors.accentGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Greeting & Assistant Persona Banner
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "$greeting, ${preferences.userDisplayName}",
                    fontSize = 26.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "I am ${preferences.assistantName}, how may I assist your day?",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            }

            // Unobtrusive Notice Banner if present
            currentNotice?.let { notice ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (notice.isError) Color(0xFF450A0A) else colors.surfaceCard
                        )
                        .border(
                            width = 1.dp,
                            color = if (notice.isError) Color(0xFFEF4444) else colors.borderLavender,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (notice.isError) Icons.Filled.Warning else Icons.Filled.Info,
                        contentDescription = null,
                        tint = if (notice.isError) Color(0xFFF87171) else colors.accentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = notice.message,
                        fontSize = 12.sp,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.dismissNotice() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            tint = colors.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Living 3D AI Energy Orb
            LivingAiOrb(
                orbState = orbState,
                orbStyle = preferences.orbStyle,
                audioAmplitude = audioAmplitude,
                reduceMotion = preferences.reduceMotion,
                size = 230.dp,
                onClick = onVoiceActionClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status indicator label under orb
            AnimatedContent(targetState = orbState, label = "orbStateLabel") { state ->
                Text(
                    text = when (state) {
                        OrbState.LISTENING -> "Listening to your voice..."
                        OrbState.THINKING -> "Thinking & processing..."
                        OrbState.SPEAKING -> "${preferences.assistantName} is speaking..."
                        OrbState.ERROR -> "Tap orb to retry voice session"
                        OrbState.IDLE -> "Tap orb to speak with ${preferences.assistantName}"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (state) {
                        OrbState.LISTENING -> colors.accentCyan
                        OrbState.SPEAKING -> colors.accentGold
                        OrbState.ERROR -> Color(0xFFF87171)
                        else -> colors.textSecondary
                    }
                )
            }

            // Real-time transcript preview and interactive visualizer during active listening
            if (orbState == OrbState.LISTENING) {
                Spacer(modifier = Modifier.height(10.dp))
                // Visualizer bars responding to audioAmplitude
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val barCount = 12
                    for (i in 0 until barCount) {
                        val factor = ((i % 4 + 1) / 4f)
                        val barHeight = (6.dp + (20f * audioAmplitude * factor).dp).coerceIn(5.dp, 24.dp)
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .width(3.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(2.dp))
                                .background(colors.accentCyan)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = liveTranscript,
                    fontSize = 13.sp,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Tap a command or speak:",
                    fontSize = 11.sp,
                    color = colors.accentGold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chips = listOf(
                        "YouTube kholo",
                        "Mummy ko call karo",
                        "Papa ko WhatsApp karo hello",
                        "Tell me a story in Hinglish",
                        "Build a website for a cafe",
                        "What is quantum computing?"
                    )
                    items(chips) { chip ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.stopListeningAndProcess(chip)
                                },
                            color = colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentCyan.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "💬 $chip",
                                fontSize = 11.sp,
                                color = colors.accentCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Shortcut Row: Text Chat & Voice Stop
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Open Chat Shortcut
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("home_open_chat_button")
                        .clickable { onNavigateToChat() },
                    color = colors.surfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = "Chat",
                            tint = colors.accentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ask Anything",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }

                // Voice Control Shortcut
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("home_mic_toggle_button")
                        .clickable { onVoiceActionClick() },
                    color = if (orbState == OrbState.LISTENING || orbState == OrbState.SPEAKING)
                        colors.accentGold.copy(alpha = 0.2f)
                    else colors.surfaceCard,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (orbState == OrbState.LISTENING || orbState == OrbState.SPEAKING)
                            colors.accentGold
                        else colors.borderLavender
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (orbState == OrbState.LISTENING) Icons.Filled.Stop
                            else if (orbState == OrbState.SPEAKING) Icons.Filled.VolumeOff
                            else Icons.Filled.Mic,
                            contentDescription = "Voice",
                            tint = colors.accentGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (orbState == OrbState.LISTENING) "Done Speaking"
                            else if (orbState == OrbState.SPEAKING) "Mute NOVA"
                            else "Voice Talk",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Quick Actions Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = colors.textPrimary
                )
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accentGold,
                    modifier = Modifier
                        .clickable { onNavigateToQuickActions() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Action horizontal carousel
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quickActionItems) { action ->
                    QuickActionCard(
                        item = action,
                        onClick = {
                            when (action.id) {
                                "app" -> onNavigateToQuickActions()
                                "call" -> onNavigateToQuickActions()
                                "msg" -> onNavigateToQuickActions()
                                "web" -> viewModel.navigateTo(
                                    Screen.WebsitePreview(
                                        title = "Portfolio Website",
                                        htmlCode = defaultPortfolioHtml
                                    )
                                )
                                "sketch" -> viewModel.navigateTo(Screen.SketchPreview("Futuristic cyberpunk city with neon lighting"))
                                "talk" -> viewModel.startVoiceListening()
                                "chat" -> onNavigateToChat()
                            }
                        }
                    )
                }
            }
        }
    }
}

data class QuickActionData(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color
)

private val quickActionItems = listOf(
    QuickActionData("app", "Open App", "Launch YouTube, WhatsApp...", Icons.Filled.Apps, Color(0xFF00F0FF)),
    QuickActionData("call", "Call Someone", "Dial direct or contacts", Icons.Filled.Call, Color(0xFF4ADE80)),
    QuickActionData("msg", "Send Message", "SMS or WhatsApp drafts", Icons.Filled.Send, Color(0xFF38BDF8)),
    QuickActionData("web", "Build Website", "Generate live web code", Icons.Filled.Code, Color(0xFFF5C542)),
    QuickActionData("sketch", "Sketch Photo", "AI visual imagery", Icons.Filled.Brush, Color(0xFFC084FC)),
    QuickActionData("talk", "Talk to NOVA", "Real-time natural voice", Icons.Filled.Mic, Color(0xFFFFD700))
)

@Composable
private fun QuickActionCard(
    item: QuickActionData,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current

    Surface(
        modifier = Modifier
            .width(140.dp)
            .height(125.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        color = colors.surfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(item.iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 10.sp,
                    color = colors.textMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ConnectionBadge(
    label: String,
    status: String,
    activeColor: Color
) {
    val isReady = status.contains("Connected", ignoreCase = true) || status.contains("Ready", ignoreCase = true)
    val isError = status.contains("Error", ignoreCase = true)

    val badgeColor = when {
        isReady -> activeColor
        isError -> Color(0xFFEF4444)
        else -> Color(0xFF71717A)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .border(0.5.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(badgeColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

val defaultPortfolioHtml = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body { font-family: -apple-system, sans-serif; background: #0B0814; color: #fff; margin: 0; padding: 20px; }
        .hero { text-align: center; padding: 40px 10px; background: linear-gradient(135deg, #1A0B2E, #251147); border-radius: 16px; border: 1px solid #3E2D6A; }
        h1 { color: #F5C542; margin-bottom: 5px; }
        p { color: #B4A8D6; }
        .btn { display: inline-block; background: #00F0FF; color: #000; padding: 12px 24px; border-radius: 25px; font-weight: bold; text-decoration: none; margin-top: 15px; }
        .card { background: #160F2B; border: 1px solid #38285C; border-radius: 12px; padding: 16px; margin-top: 20px; }
    </style>
</head>
<body>
    <div class="hero">
        <h1>Nova Creative Agency</h1>
        <p>Crafting intelligent digital products with AI</p>
        <a href="#" class="btn">Explore Work</a>
    </div>
    <div class="card">
        <h3>🚀 Mobile App Engineering</h3>
        <p>Native Android with Kotlin & Jetpack Compose.</p>
    </div>
    <div class="card">
        <h3>✨ AI Companion Architecture</h3>
        <p>Gemini multimodal and Groq low-latency inference.</p>
    </div>
</body>
</html>
""".trimIndent()
