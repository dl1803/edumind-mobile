package com.edumind.app.data.model.response

import kotlin.math.roundToLong

data class SubtitleSegment(
    val startMs: Long,
    val endMs: Long,
    val text: String
)

fun TranscriptSegmentDto.toSubtitleSegment(): SubtitleSegment = SubtitleSegment(
    startMs = (start * 1000f).roundToLong().coerceAtLeast(0L),
    endMs = (end * 1000f).roundToLong().coerceAtLeast(0L),
    text = text?.trim().orEmpty()
)

