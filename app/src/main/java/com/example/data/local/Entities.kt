package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastMessageSnippet: String = "",
    val recap: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val hasAttachment: Boolean = false,
    val attachmentUri: String? = null,
    val attachmentType: String? = null, // "image", "file"
    val isError: Boolean = false,
    val suggestedActionJson: String? = null
)
