package com.edumind.app.repository

import com.edumind.app.data.model.response.ExerciseDto
import com.edumind.app.data.model.response.ExerciseEntry
import com.edumind.app.data.model.response.ExerciseQuestionDto
import com.edumind.app.data.model.response.ExerciseQuestionType
import com.edumind.app.data.model.response.QuizOptionDto
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeExerciseRepository @Inject constructor() : ExerciseRepository {

    override suspend fun getLessonExercise(lessonId: String): Result<ExerciseEntry?> {
        delay(NETWORK_DELAY_MS)
        val exercise = EXERCISES.getOrNull(lessonId.hashCode().mod(BUCKET_COUNT))
        return Result.success(exercise?.let { ExerciseEntry(exerciseId = it.id, title = it.title) })
    }

    override suspend fun getExercise(exerciseId: String): Result<ExerciseDto> {
        delay(NETWORK_DELAY_MS)
        val exercise = EXERCISES.firstOrNull { it.id == exerciseId }
            ?: return Result.failure(NoSuchElementException("Không tìm thấy bài tập $exerciseId"))
        return Result.success(exercise)
    }

    private companion object {
        const val NETWORK_DELAY_MS = 400L
        const val BUCKET_COUNT = 4

        val EXERCISES = listOf(
            ExerciseDto(
                id = "exercise-mock-01",
                title = "Bài tập cuối bài: Biến và kiểu dữ liệu",
                lessonId = "mock-lesson-01",
                timeLimit = 900,
                passingScore = 70,
                maxAttempts = 3,
                attemptsUsed = 1,
                totalQuestions = 5,
                questions = listOf(
                    question(
                        id = "ex1-q1",
                        order = 1,
                        type = ExerciseQuestionType.SINGLE_CHOICE,
                        content = "Kiểu dữ liệu nào dưới đây là bất biến (immutable) trong Python?",
                        options = options("List", "Tuple", "Dict", "Set")
                    ),
                    question(
                        id = "ex1-q2",
                        order = 2,
                        type = ExerciseQuestionType.MULTIPLE_CHOICE,
                        content = "Chọn tất cả kiểu dữ liệu có thể thay đổi nội dung sau khi tạo.",
                        options = options("List", "Tuple", "Dict", "Set")
                    ),
                    question(
                        id = "ex1-q3",
                        order = 3,
                        type = ExerciseQuestionType.TRUE_FALSE,
                        content = "Có thể gán lại một ký tự của chuỗi str bằng chỉ số, ví dụ s[0] = 'a'.",
                        options = options("Đúng", "Sai")
                    ),
                    question(
                        id = "ex1-q4",
                        order = 4,
                        type = ExerciseQuestionType.SINGLE_CHOICE,
                        content = "Hàm nào trả về số phần tử của một danh sách?",
                        options = options("size()", "len()", "count()", "length()")
                    ),
                    question(
                        id = "ex1-q5",
                        order = 5,
                        type = ExerciseQuestionType.ESSAY,
                        content = "Trình bày sự khác biệt giữa kiểu dữ liệu mutable và immutable trong Python. Nêu một trường hợp nên ưu tiên dùng tuple thay vì list."
                    )
                )
            ),
            ExerciseDto(
                id = "exercise-mock-02",
                title = "Bài tập cuối bài: Câu lệnh điều kiện",
                lessonId = "mock-lesson-02",
                timeLimit = 0,
                passingScore = 60,
                maxAttempts = 0,
                attemptsUsed = 0,
                totalQuestions = 3,
                questions = listOf(
                    question(
                        id = "ex2-q1",
                        order = 1,
                        type = ExerciseQuestionType.SINGLE_CHOICE,
                        content = "Từ khóa nào dùng để thêm nhánh điều kiện thay thế sau if?",
                        options = options("elif", "else if", "elseif", "otherwise")
                    ),
                    question(
                        id = "ex2-q2",
                        order = 2,
                        type = ExerciseQuestionType.TRUE_FALSE,
                        content = "Trong Python, khối lệnh sau if được xác định bằng thụt lề.",
                        options = options("Đúng", "Sai")
                    ),
                    question(
                        id = "ex2-q3",
                        order = 3,
                        type = ExerciseQuestionType.MULTIPLE_CHOICE,
                        content = "Chọn các biểu thức có giá trị True.",
                        options = options("3 > 2", "'a' == 'b'", "not False", "1 == 2")
                    )
                )
            ),
            ExerciseDto(
                id = "exercise-mock-03",
                title = "Bài tập cuối bài: Vòng lặp",
                lessonId = "mock-lesson-03",
                timeLimit = 600,
                passingScore = 80,
                maxAttempts = 2,
                attemptsUsed = 2,
                totalQuestions = 2,
                questions = listOf(
                    question(
                        id = "ex3-q1",
                        order = 1,
                        type = ExerciseQuestionType.SINGLE_CHOICE,
                        content = "Vòng lặp nào dùng khi biết trước tập phần tử cần duyệt?",
                        options = options("for", "while", "do-while", "loop")
                    ),
                    question(
                        id = "ex3-q2",
                        order = 2,
                        type = ExerciseQuestionType.SINGLE_CHOICE,
                        content = "Từ khóa nào dừng hẳn vòng lặp hiện tại?",
                        options = options("continue", "break", "pass", "exit")
                    )
                )
            )
        )

        fun options(vararg contents: String): List<QuizOptionDto> =
            contents.mapIndexed { index, content ->
                QuizOptionDto(key = ('A' + index).toString(), content = content)
            }

        fun question(
            id: String,
            order: Int,
            type: ExerciseQuestionType,
            content: String,
            options: List<QuizOptionDto>? = null
        ): ExerciseQuestionDto = ExerciseQuestionDto(
            id = id,
            type = type,
            content = content,
            options = options,
            order = order
        )
    }
}
