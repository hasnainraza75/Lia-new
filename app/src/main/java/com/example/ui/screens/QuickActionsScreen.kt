package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.device.ActionExecutionResult
import com.example.data.local.ContactItem
import com.example.data.local.DeviceAction
import com.example.ui.theme.LocalCustomColors
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

data class QuickActionCardItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun QuickActionsScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalCustomColors.current
    val preferences by viewModel.preferences.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Dialog states for Quick Actions
    var activeActionModal by remember { mutableStateOf<String?>(null) }
    var inputQuery by remember { mutableStateOf("") }
    var secondaryInput by remember { mutableStateOf("") }
    var contactsResults by remember { mutableStateOf<List<ContactItem>>(emptyList()) }

    val cards = remember {
        listOf(
            QuickActionCardItem("open_app", "Open an App", "Launch YouTube, WhatsApp, Settings or games", Icons.Filled.Apps, Color(0xFF00F0FF)),
            QuickActionCardItem("call", "Call Someone", "Dial phone number or search contacts", Icons.Filled.Call, Color(0xFF4ADE80)),
            QuickActionCardItem("message", "Send Message", "Prepare SMS or WhatsApp draft safely", Icons.Filled.Send, Color(0xFF38BDF8)),
            QuickActionCardItem("build_web", "Build Website", "AI generated interactive HTML/CSS", Icons.Filled.Code, Color(0xFFF5C542)),
            QuickActionCardItem("sketch", "Sketch Photo", "Generate creative visual imagery", Icons.Filled.Brush, Color(0xFFC084FC)),
            QuickActionCardItem("talk", "Talk to ${preferences.assistantName}", "Start real-time natural voice conversation", Icons.Filled.Mic, Color(0xFFFFD700)),
            QuickActionCardItem("ask", "Ask Anything", "Instant AI knowledge and problem solving", Icons.Filled.ChatBubble, Color(0xFFF472B6))
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
                        text = "Quick Actions",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Native phone controls & AI utilities",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2-Column Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(cards) { card ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .testTag("quick_action_${card.id}")
                            .clickable {
                                when (card.id) {
                                    "open_app" -> {
                                        inputQuery = ""
                                        activeActionModal = "open_app"
                                    }
                                    "call" -> {
                                        inputQuery = ""
                                        contactsResults = emptyList()
                                        activeActionModal = "call"
                                    }
                                    "message" -> {
                                        inputQuery = ""
                                        secondaryInput = ""
                                        contactsResults = emptyList()
                                        activeActionModal = "message"
                                    }
                                    "build_web" -> {
                                        viewModel.navigateTo(
                                            Screen.WebsitePreview(
                                                title = "Creative Portfolio",
                                                htmlCode = defaultPortfolioHtml
                                            )
                                        )
                                    }
                                    "sketch" -> {
                                        viewModel.navigateTo(Screen.SketchPreview("Futuristic purple cosmic nebula"))
                                    }
                                    "talk" -> {
                                        viewModel.startVoiceListening()
                                        onNavigateBack()
                                    }
                                    "ask" -> {
                                        onNavigateToChat()
                                    }
                                }
                            },
                        color = colors.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(card.accentColor.copy(alpha = 0.15f))
                                    .border(1.dp, card.accentColor.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = card.icon,
                                    contentDescription = card.title,
                                    tint = card.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = card.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = card.description,
                                    fontSize = 11.sp,
                                    color = colors.textMuted,
                                    lineHeight = 15.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Modal: Open App
        if (activeActionModal == "open_app") {
            AlertDialog(
                onDismissRequest = { activeActionModal = null },
                title = { Text("Open Application", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Enter the name of an installed app (e.g. YouTube, WhatsApp, Chrome):", fontSize = 13.sp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = { inputQuery = it },
                            placeholder = { Text("e.g. YouTube") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val q = inputQuery
                            activeActionModal = null
                            coroutineScope.launch {
                                val result = viewModel.deviceActionManager.openApp(q)
                                when (result) {
                                    is ActionExecutionResult.Success -> viewModel.showNotice(result.message)
                                    is ActionExecutionResult.Error -> viewModel.showNotice(result.error, isError = true)
                                    else -> {}
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentCyan)
                    ) {
                        Text("Open App", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeActionModal = null }) {
                        Text("Cancel", color = colors.textMuted)
                    }
                },
                containerColor = colors.surfaceCard,
                shape = RoundedCornerShape(18.dp)
            )
        }

        // Action Modal: Call Someone
        if (activeActionModal == "call") {
            AlertDialog(
                onDismissRequest = { activeActionModal = null },
                title = { Text("Call Someone", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Search contact name or enter a direct phone number:", fontSize = 13.sp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = {
                                inputQuery = it
                                coroutineScope.launch {
                                    if (it.length >= 2) {
                                        contactsResults = viewModel.deviceActionManager.searchContacts(it)
                                    }
                                }
                            },
                            placeholder = { Text("e.g. Mummy or 9876543210") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        if (contactsResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Matched Contacts:", fontSize = 11.sp, color = colors.accentGold)
                            contactsResults.take(3).forEach { c ->
                                Text(
                                    text = "• ${c.name}: ${c.phoneNumber}",
                                    fontSize = 12.sp,
                                    color = colors.textPrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            inputQuery = c.phoneNumber
                                        }
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val num = inputQuery
                            activeActionModal = null
                            viewModel.deviceActionManager.dialPhoneNumber(num)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ADE80))
                    ) {
                        Text("Dial", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeActionModal = null }) {
                        Text("Cancel", color = colors.textMuted)
                    }
                },
                containerColor = colors.surfaceCard,
                shape = RoundedCornerShape(18.dp)
            )
        }

        // Action Modal: Send Message
        if (activeActionModal == "message") {
            AlertDialog(
                onDismissRequest = { activeActionModal = null },
                title = { Text("Send Message Draft", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Recipient contact or phone number:", fontSize = 12.sp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = { inputQuery = it },
                            placeholder = { Text("e.g. Papa or 9876543210") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Draft Message (you will review and send manually):", fontSize = 12.sp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = secondaryInput,
                            onValueChange = { secondaryInput = it },
                            placeholder = { Text("e.g. Running 10 mins late!") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val recipient = inputQuery
                                val msg = secondaryInput
                                activeActionModal = null
                                viewModel.deviceActionManager.prepareWhatsApp(recipient, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Text("WhatsApp", color = Color.White)
                        }
                        Button(
                            onClick = {
                                val recipient = inputQuery
                                val msg = secondaryInput
                                activeActionModal = null
                                viewModel.deviceActionManager.prepareSms(recipient, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accentCyan)
                        ) {
                            Text("SMS", color = Color.Black)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeActionModal = null }) {
                        Text("Cancel", color = colors.textMuted)
                    }
                },
                containerColor = colors.surfaceCard,
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}
