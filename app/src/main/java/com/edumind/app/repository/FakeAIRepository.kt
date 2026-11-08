package com.edumind.app.repository

import com.edumind.app.data.model.response.ChatMessage
import com.edumind.app.data.model.response.ChatRole
import com.edumind.app.data.model.response.Citation
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAIRepository @Inject constructor() : AIRepository {

    override suspend fun ask(lessonId: String, question: String): Result<ChatMessage> {
        delay(REPLY_DELAY_MS)
        if (ERROR_KEYWORDS.any { question.contains(it, ignoreCase = true) }) {
            return Result.failure(IOException("Mock AI failure"))
        }
        val citations = buildList {
            add(Citation(timestampMs = 83_000L, label = "01:23"))
            if (question.length >= LONG_QUESTION_LENGTH) {
                add(Citation(timestampMs = 290_000L, label = "04:50"))
            }
        }
        return Result.success(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                role = ChatRole.AI,
                text = REPLY_TEXT,
                citations = citations
            )
        )
    }

    companion object {
        private const val REPLY_DELAY_MS = 1_200L
        private val ERROR_KEYWORDS = listOf("lỗi", "error")
        private const val LONG_QUESTION_LENGTH = 20
        private const val REPLY_TEXT =
            "Trong Python, kiểu dữ liệu của biến được xác định tự động khi bạn gán giá trị nên không cần khai báo kiểu trước. Đây là câu trả lời mẫu dùng để kiểm thử giao diện."
    }
}

