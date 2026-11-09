package com.edumind.app.repository

import com.edumind.app.data.model.response.LectureSummary

interface SummaryRepository {
    suspend fun getSummary(lessonId: String): Result<LectureSummary?>
    suspend fun generateSummary(lessonId: String): Result<LectureSummary>
}

