package com.edumind.app.repository

import com.edumind.app.data.model.response.SubtitleSegment

interface SubtitleRepository {
    suspend fun getSegments(lessonId: String): List<SubtitleSegment>
}

