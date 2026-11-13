package com.edumind.app.repository

import android.content.Context
import com.edumind.app.data.model.response.SubtitleSegment
import com.edumind.app.util.subtitle.VttParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSubtitleRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SubtitleRepository {

    override suspend fun getSegments(lessonId: String): List<SubtitleSegment> {
        if (lessonId == NO_SUBTITLE_LESSON_ID) return emptyList()
        val content = withContext(Dispatchers.IO) {
            context.assets.open(SAMPLE_ASSET_PATH)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
        }
        return withContext(Dispatchers.Default) { VttParser.parse(content) }
    }

    companion object {
        const val NO_SUBTITLE_LESSON_ID = "no_subtitle_lesson"
        private const val SAMPLE_ASSET_PATH = "subtitles/sample_vi.vtt"
    }
}
