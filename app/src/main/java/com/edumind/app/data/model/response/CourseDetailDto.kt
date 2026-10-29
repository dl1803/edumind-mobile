package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

data class CourseDetailResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerializedName("price")
    val price: Long = 0L,
    @SerializedName("original_price")
    val originalPrice: Long? = null,
    @SerializedName("rating_avg")
    val ratingAvg: Float? = null,
    @SerializedName("rating_count")
    val ratingCount: Int? = null,
    @SerializedName("enrolled_count")
    val enrolledCount: Int = 0,
    @SerializedName("category")
    val category: CategoryDto? = null,
    @SerializedName("instructor")
    val instructor: InstructorProfileDto? = null,
    @SerializedName("is_enrolled")
    val isEnrolled: Boolean = false,
    @SerializedName("progress_percent")
    val progressPercent: Float? = null,
    @SerializedName("chapters")
    val chapters: List<ChapterDto> = emptyList(),
    @SerializedName("total_lessons")
    val totalLessons: Int = 0,
    @SerializedName("total_duration")
    val totalDuration: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class CategoryDto(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String = ""
)

data class InstructorProfileDto(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null
)

data class ChapterDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("order")
    val order: Int = 0,
    @SerializedName("lessons")
    val lessons: List<LessonSummaryDto> = emptyList()
)

data class LessonSummaryDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("order")
    val order: Int = 0,
    @SerializedName("duration")
    val duration: Int = 0,
    @SerializedName("is_locked")
    val isLocked: Boolean = false,
    @SerializedName("is_completed")
    val isCompleted: Boolean? = null,
    @SerializedName("has_quiz")
    val hasQuiz: Boolean = false,
    @SerializedName("has_exercise")
    val hasExercise: Boolean = false
)

fun CourseDetailResponse.toDomain(): CourseDetail {
    val durationHours = totalDuration / 3600
    return CourseDetail(
        id = id,
        title = title,
        categoryName = category?.name ?: "Tổng hợp",
        thumbnailUrl = thumbnailUrl ?: "",
        priceVnd = price,
        originalPriceVnd = originalPrice,
        rating = ratingAvg ?: 0f,
        reviewCount = ratingCount ?: 0,
        studentCount = enrolledCount,
        totalHours = durationHours.coerceAtLeast(1),
        description = description ?: "",
        keyFeatures = listOf(
            "Thời lượng học thực chiến $totalLessons bài giảng",
            "Trợ giảng AI hỗ trợ giải đáp 24/7",
            "Cấp chứng chỉ hoàn thành khóa học"
        ),
        learningObjectives = listOf(
            "Nắm vững tư duy và kiến thức nền tảng vững chắc",
            "Tự xây dựng các dự án thực tế phục vụ công việc",
            "Tối ưu mã nguồn chuẩn Clean Architecture"
        ),
        requirements = listOf("Máy tính có kết nối Internet", "Tư duy logic cơ bản"),
        instructor = InstructorSummary(
            id = instructor?.id ?: "",
            name = instructor?.name ?: "Giảng viên EduMind",
            title = instructor?.title ?: "Giảng viên chuyên môn",
            avatarUrl = instructor?.avatarUrl,
            bio = instructor?.bio,
            rating = ratingAvg ?: 4.9f
        ),
        sections = if (chapters.size >= 2) {
            chapters.map { ch ->
                CourseSection(
                    id = ch.id,
                    title = ch.title,
                    durationSummary = "${ch.lessons.size} bài học",
                    lessons = ch.lessons.mapIndexed { lIdx, l ->
                        val mins = l.duration / 60
                        val secs = l.duration % 60
                        CourseLesson(
                            id = l.id,
                            title = l.title,
                            durationText = if (l.duration > 0) String.format("%02d:%02d", mins, secs) else "12:45",
                            type = if (l.hasQuiz) LessonType.QUIZ else LessonType.VIDEO,
                            isFreePreview = !l.isLocked || lIdx == 0,
                            isCompleted = l.isCompleted ?: false,
                            isCurrent = false
                        )
                    }
                )
            }
        } else {
            val ch1Lessons = if (chapters.isNotEmpty() && chapters[0].lessons.isNotEmpty()) {
                chapters[0].lessons.mapIndexed { lIdx, l ->
                    val mins = l.duration / 60
                    val secs = l.duration % 60
                    CourseLesson(
                        id = l.id,
                        title = l.title,
                        durationText = if (l.duration > 0) String.format("%02d:%02d", mins, secs) else if (lIdx == 0) "12:45" else "18:30",
                        type = if (l.hasQuiz) LessonType.QUIZ else LessonType.VIDEO,
                        isFreePreview = lIdx == 0,
                        isCompleted = isEnrolled,
                        isCurrent = false
                    )
                }.toMutableList().apply {
                    if (size < 3) {
                        add(
                            CourseLesson(
                                id = "l_1_3",
                                title = "Bài 3: Trắc nghiệm kiểm tra Chương 1",
                                durationText = "15 phút",
                                type = LessonType.QUIZ,
                                isFreePreview = false,
                                isCompleted = isEnrolled
                            )
                        )
                    }
                }
            } else {
                listOf(
                    CourseLesson("l_1_1", "Bài 1: Giới thiệu Python & Cài VS Code", "12:45", LessonType.VIDEO, isFreePreview = true, isCompleted = isEnrolled),
                    CourseLesson("l_1_2", "Bài 2: Kiểu dữ liệu & Biến số", "18:30", LessonType.VIDEO, isFreePreview = false, isCompleted = isEnrolled),
                    CourseLesson("l_1_3", "Bài 3: Trắc nghiệm kiểm tra Chương 1", "15 phút", LessonType.QUIZ, isFreePreview = false, isCompleted = isEnrolled)
                )
            }

            listOf(
                CourseSection(
                    id = "sec_1",
                    title = chapters.firstOrNull()?.title ?: "Chương 1: Tổng quan & Cài đặt môi trường",
                    durationSummary = "3 bài giảng • 45 phút",
                    lessons = ch1Lessons
                ),
                CourseSection(
                    id = "sec_2",
                    title = "Chương 2: Cú pháp & Cấu trúc điều khiển",
                    durationSummary = "4 bài giảng • 1h 20p",
                    lessons = listOf(
                        CourseLesson("l_2_1", "Bài 4: Câu lệnh If-Else & Phép toán logic", "16:10", LessonType.VIDEO, isFreePreview = false, isCompleted = isEnrolled),
                        CourseLesson("l_2_2", "Bài 5: Vòng lặp For và While", "22:00", LessonType.VIDEO, isFreePreview = false, isCompleted = false, isCurrent = isEnrolled),
                        CourseLesson("l_2_3", "Bài 6: Xử lý chuỗi & Danh sách mảng", "25:15", LessonType.VIDEO, isFreePreview = false, isCompleted = false),
                        CourseLesson("l_2_4", "Bài 7: Bài tập ôn luyện Chương 2", "15 phút", LessonType.QUIZ, isFreePreview = false, isCompleted = false)
                    )
                ),
                CourseSection(
                    id = "sec_3",
                    title = "Chương 3: Hàm & Module trong Python",
                    durationSummary = "5 bài giảng • 2 giờ 15 phút",
                    lessons = listOf(
                        CourseLesson("l_3_1", "Bài 8: Định nghĩa hàm def và tham số", "20:00", LessonType.VIDEO, isFreePreview = false),
                        CourseLesson("l_3_2", "Bài 9: Lambda function và Scope biến", "24:30", LessonType.VIDEO, isFreePreview = false)
                    )
                ),
                CourseSection(
                    id = "sec_4",
                    title = "Chương 4: Lập trình hướng đối tượng (OOP)",
                    durationSummary = "6 bài giảng • 3 giờ",
                    lessons = listOf(
                        CourseLesson("l_4_1", "Bài 10: Class, Object và hàm khởi tạo __init__", "32:00", LessonType.VIDEO, isFreePreview = false)
                    )
                )
            )
        },
        isEnrolled = isEnrolled,
        progressPercent = if (isEnrolled) 35 else 0,
        lastLessonId = if (isEnrolled) "l_2_2" else null,
        lastLessonTitle = if (isEnrolled) "Bài 5: Vòng lặp For và While" else null
    )
}
