package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DeviceAction
import com.example.data.local.OrbState
import com.example.data.local.ProviderType
import com.example.ui.components.LivingAiOrb
import com.example.ui.theme.LocalCustomColors
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalCustomColors.current
    val preferences by viewModel.preferences.collectAsState()
    val messages by viewModel.currentMessages.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val pendingAction by viewModel.pendingAction.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Android Photo Picker launcher (zero-permission)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            capturedBitmap = null
        }
    }

    // Camera capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            // Save to temp file for URI
            try {
                val tempFile = File.createTempFile("captured_photo", ".jpg", context.cacheDir)
                FileOutputStream(tempFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                selectedImageUri = Uri.fromFile(tempFile)
            } catch (_: Exception) {}
        }
    }

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Confirmation Dialog for Consequential Actions (Calls, WhatsApp drafts)
    pendingAction?.let { action ->
        ActionConfirmationDialog(
            action = action,
            onConfirm = { viewModel.confirmPendingAction() },
            onDismiss = { viewModel.dismissPendingAction() }
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
                .padding(bottom = 76.dp) // space above bottom nav
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceHighlight)
                            .border(1.dp, colors.accentGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        LivingAiOrb(
                            orbState = orbState,
                            orbStyle = preferences.orbStyle,
                            audioAmplitude = 0f,
                            size = 32.dp,
                            onClick = {}
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = preferences.assistantName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = colors.textPrimary
                        )
                        val activeModel = if (preferences.chatProvider == ProviderType.GEMINI) preferences.geminiModel else preferences.groqModel
                        Text(
                            text = "${preferences.chatProvider.displayName} ($activeModel) • ${preferences.personality.displayName}",
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = { viewModel.startNewConversation() },
                        modifier = Modifier.testTag("chat_new_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddComment,
                            contentDescription = "New Chat",
                            tint = colors.accentGold
                        )
                    }
                    IconButton(
                        onClick = { viewModel.clearAllHistory() },
                        modifier = Modifier.testTag("chat_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Clear",
                            tint = colors.textMuted
                        )
                    }
                }
            }

            HorizontalDivider(color = colors.borderLavender.copy(alpha = 0.5f))

            // Messages List or Welcome Banner
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    EmptyChatGreeting(
                        assistantName = preferences.assistantName,
                        userDisplayName = preferences.userDisplayName,
                        onSampleClick = { sample ->
                            inputText = sample
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages) { message ->
                            ChatMessageBubble(
                                message = message,
                                assistantName = preferences.assistantName,
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("${preferences.assistantName} Response", message.content))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                onShare = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        putExtra(Intent.EXTRA_TEXT, message.content)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share ${preferences.assistantName} Response"))
                                },
                                onRetry = {
                                    viewModel.sendChatMessage(message.content, isVoiceTurn = false, context = context)
                                }
                            )
                        }

                        if (orbState == OrbState.THINKING) {
                            item {
                                ThinkingIndicatorBubble(assistantName = preferences.assistantName)
                            }
                        }
                    }
                }
            }

            // Attachment Preview Bar if image is selected
            if (selectedImageUri != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceCard)
                        .border(1.dp, colors.borderLavender, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (capturedBitmap != null) {
                            Image(
                                bitmap = capturedBitmap!!.asImageBitmap(),
                                contentDescription = "Captured Photo",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Attached Image",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Photo attached",
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    }
                    IconButton(onClick = {
                        selectedImageUri = null
                        capturedBitmap = null
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove",
                            tint = colors.textMuted
                        )
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(26.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera Action
                    IconButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "Camera",
                            tint = colors.accentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Photo Picker Action
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = "Attach Image",
                            tint = colors.accentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Text Input
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        placeholder = {
                            Text(
                                text = "Ask ${preferences.assistantName} anything...",
                                fontSize = 14.sp,
                                color = colors.textMuted
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        maxLines = 4
                    )

                    // Send or Mic button
                    if (inputText.isNotBlank() || selectedImageUri != null) {
                        IconButton(
                            onClick = {
                                val textToSend = inputText
                                val imgToSend = selectedImageUri
                                inputText = ""
                                selectedImageUri = null
                                capturedBitmap = null
                                viewModel.sendChatMessage(
                                    text = textToSend,
                                    isVoiceTurn = false,
                                    imageUri = imgToSend,
                                    context = context
                                )
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.accentGold)
                                .testTag("chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                viewModel.toggleVoiceSession()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (orbState == OrbState.LISTENING) colors.accentCyan
                                    else colors.surfaceHighlight
                                )
                                .testTag("chat_mic_button")
                        ) {
                            Icon(
                                imageVector = if (orbState == OrbState.LISTENING) Icons.Filled.GraphicEq else Icons.Filled.Mic,
                                contentDescription = "Voice Input",
                                tint = if (orbState == OrbState.LISTENING) Color.Black else colors.accentGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageEntity,
    assistantName: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRetry: () -> Unit
) {
    val colors = LocalCustomColors.current
    val isUser = message.role == "user"
    val timeStr = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.orbPrimary)
                    .align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = assistantName.take(1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = when {
                    message.isError -> Color(0xFF450A0A)
                    isUser -> colors.surfaceHighlight
                    else -> colors.surfaceCard
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                        message.isError -> Color(0xFFEF4444)
                        isUser -> colors.accentCyan.copy(alpha = 0.4f)
                        else -> colors.borderLavender
                    }
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.hasAttachment && message.attachmentUri != null) {
                        AsyncImage(
                            model = message.attachmentUri,
                            contentDescription = "Attachment",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = message.content,
                        fontSize = 14.sp,
                        color = colors.textPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeStr,
                            fontSize = 10.sp,
                            color = colors.textMuted
                        )
                        if (!isUser && message.modelUsed != null) {
                            Text(
                                text = message.modelUsed,
                                fontSize = 9.sp,
                                color = colors.accentGold
                            )
                        }
                    }
                }
            }

            // Action toolbar for assistant message
            if (!isUser) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy",
                        tint = colors.textMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onCopy() }
                    )
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = colors.textMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onShare() }
                    )
                    if (message.isError) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Retry",
                            tint = Color(0xFFF87171),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onRetry() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinkingIndicatorBubble(assistantName: String) {
    val colors = LocalCustomColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(colors.orbPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = assistantName.take(1),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = colors.surfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = colors.accentCyan,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$assistantName is thinking...",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun EmptyChatGreeting(
    assistantName: String,
    userDisplayName: String,
    onSampleClick: (String) -> Unit
) {
    val colors = LocalCustomColors.current
    val samplePrompts = listOf(
        "YouTube kholo aur relaxing music chalao",
        "Mummy ko call karo",
        "Write a clean modern website for a cafe",
        "Explain quantum computing in simple Hinglish",
        "Papa ko WhatsApp draft: Main 10 baje tak pahunchunga"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome, $userDisplayName",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "I'm $assistantName, ready to converse, call, message or build.",
            fontSize = 13.sp,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            samplePrompts.forEach { prompt ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSampleClick(prompt) },
                    color = colors.surfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = prompt,
                        fontSize = 12.sp,
                        color = colors.accentGold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionConfirmationDialog(
    action: DeviceAction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalCustomColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (action) {
                    is DeviceAction.CallPhone -> "Confirm Phone Call"
                    is DeviceAction.SendMessage -> if (action.isWhatsApp) "Confirm WhatsApp Message" else "Confirm SMS"
                    else -> "Confirm Action"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        },
        text = {
            Column {
                when (action) {
                    is DeviceAction.CallPhone -> {
                        Text(
                            text = "Call ${action.contactName} at ${action.phoneNumber}?",
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }
                    is DeviceAction.SendMessage -> {
                        Text(
                            text = "Recipient: ${action.contactName} (${action.phoneNumber ?: "Selected Contact"})\n\nDraft Message:\n\"${action.message}\"",
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold)
            ) {
                Text("Proceed", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textMuted)
            }
        },
        containerColor = colors.surfaceCard,
        shape = RoundedCornerShape(18.dp)
    )
}
