package com.edumind.app.data.model.response

data class MyCourse(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val instructorName: String,
    val progressPercent: Int,
    val totalLessons: Int = 20,
    val completedLessons: Int = 0,
    val nextLessonTitle: String = "",
    val nextLessonId: String = "",
    val categoryName: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0
)
