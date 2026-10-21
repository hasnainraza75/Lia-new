package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.ui.theme.LocalCustomColors
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val colors = LocalCustomColors.current
    val conversations by viewModel.conversationsList.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val filtered = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.lastMessageSnippet.contains(searchQuery, ignoreCase = true)
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Clear All Chat History?",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all saved chats from local storage. This action cannot be undone.",
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chat History",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                if (conversations.isNotEmpty()) {
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.testTag("history_clear_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteSweep,
                            contentDescription = "Clear All",
                            tint = Color(0xFFF87171)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = colors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_search_input"),
                        placeholder = {
                            Text(text = "Search conversations...", color = colors.textMuted, fontSize = 14.sp)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Conversation List
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.HistoryToggleOff,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching conversations" else "No chat history yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Conversations with LIA will be archived here.",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { conversation ->
                        ConversationItemCard(
                            conversation = conversation,
                            onClick = { viewModel.selectConversation(conversation.id) },
                            onDelete = { viewModel.deleteConversation(conversation.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationItemCard(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalCustomColors.current
    val dateStr = remember(conversation.updatedAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(conversation.updatedAt))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = colors.surfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (conversation.lastMessageSnippet.isNotBlank()) {
                    Text(
                        text = conversation.lastMessageSnippet,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = colors.accentGold
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete",
                    tint = colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
