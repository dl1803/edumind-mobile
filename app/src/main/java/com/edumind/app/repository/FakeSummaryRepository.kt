package com.edumind.app.repository

import com.edumind.app.data.model.response.LectureSummary
import com.edumind.app.data.model.response.SummarySection
import kotlinx.coroutines.delay
import java.io.IOException
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSummaryRepository @Inject constructor() : SummaryRepository {

    private val cache = ConcurrentHashMap<String, LectureSummary>()
    private val failedLessons = ConcurrentHashMap.newKeySet<String>()

    override suspend fun getSummary(lessonId: String): Result<LectureSummary?> =
        Result.success(cache[lessonId])

    override suspend fun generateSummary(lessonId: String): Result<LectureSummary> {
        delay(GENERATE_DELAY_MS)
        if (lessonId == ERROR_LESSON_ID && failedLessons.add(lessonId)) {
            return Result.failure(IOException("Mock summary failure"))
        }
        val generatedAt = Instant.now().toString()
        if (lessonId == EMPTY_LESSON_ID) {
            return Result.success(
                LectureSummary(
                    lessonId = lessonId,
                    keyPoints = emptyList(),
                    sections = emptyList(),
                    generatedAt = generatedAt
                )
            )
        }
        val summary = LectureSummary(
            lessonId = lessonId,
            keyPoints = MOCK_KEY_POINTS,
            sections = MOCK_SECTIONS,
            isCached = false,
            generatedAt = generatedAt
        )
        cache[lessonId] = summary.copy(isCached = true)
        return Result.success(summary)
    }

    companion object {
        const val EMPTY_LESSON_ID = "summary_empty_lesson"
        const val ERROR_LESSON_ID = "summary_error_lesson"
        private const val GENERATE_DELAY_MS = 1500L

        private val MOCK_KEY_POINTS = listOf(
            "Biến là tên gọi dùng để lưu trữ dữ liệu trong bộ nhớ.",
            "Python có bốn kiểu dữ liệu cơ bản: int, float, str và bool.",
            "Có thể ép kiểu bằng int(), float(), str() và kiểm tra kiểu bằng type()."
        )

        private val MOCK_SECTIONS = listOf(
            SummarySection(
                id = "section_1",
                title = "Khái niệm biến và cách đặt tên",
                content = "Biến được tạo khi gán giá trị lần đầu. Tên biến chỉ gồm chữ, số, dấu gạch dưới và không được bắt đầu bằng số.",
                startMs = 135_000L
            ),
            SummarySection(
                id = "section_2",
                title = "Các kiểu dữ liệu cơ bản",
                content = "Kiểu của biến được xác định tự động theo giá trị được gán: số nguyên, số thực, chuỗi và logic.",
                startMs = 460_000L
            ),
            SummarySection(
                id = "section_3",
                title = "Ép kiểu và kiểm tra kiểu",
                content = "Dùng các hàm int(), float(), str() để chuyển đổi giữa các kiểu và type() để biết kiểu hiện tại của một biến.",
                startMs = 710_000L
            )
        )
    }
}

