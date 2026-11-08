package com.edumind.app.repository

import com.edumind.app.data.model.response.ChatMessage

interface AIRepository {
    suspend fun ask(lessonId: String, question: String): Result<ChatMessage>
}

