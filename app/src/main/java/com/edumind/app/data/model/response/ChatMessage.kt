package com.edumind.app.data.model.response

enum class ChatRole { USER, AI }

enum class ChatMessageStatus { SENDING, SENT, ERROR }

data class Citation(
    val timestampMs: Long,
    val label: String
)

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val text: String,
    val citations: List<Citation> = emptyList(),
    val status: ChatMessageStatus = ChatMessageStatus.SENT
)

