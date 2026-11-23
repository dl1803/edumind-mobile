package com.edumind.app.util.quiz

import com.edumind.app.data.model.response.QuizMarker

class QuizTriggerManager {

    private var markers: List<QuizMarker> = emptyList()
    private val answeredIds = mutableSetOf<String>()
    private val triggeredIds = mutableSetOf<String>()
    private val skippedIds = mutableSetOf<String>()
    private var lastPositionMs: Long? = null

    val skippedQuizIds: Set<String>
        get() = skippedIds.toSet()

    fun load(newMarkers: List<QuizMarker>) {
        markers = newMarkers.sortedBy { it.timestampSec }
        answeredIds.clear()
        answeredIds.addAll(newMarkers.filter { it.isAnswered }.map { it.quizId })
        triggeredIds.clear()
        skippedIds.clear()
        lastPositionMs = null
    }

    fun onPositionChanged(positionMs: Long): QuizMarker? {
        val previous = lastPositionMs
        lastPositionMs = positionMs
        if (markers.isEmpty()) return null
        if (previous == null) {
            skippedIds.addAll(crossedMarkers(BASELINE_POSITION_MS, positionMs).map { it.quizId })
            return null
        }
        val delta = positionMs - previous
        return when {
            delta > SEEK_JUMP_THRESHOLD_MS -> {
                skippedIds.addAll(crossedMarkers(previous, positionMs).map { it.quizId })
                null
            }
            delta < 0L -> {
                markers.filter { it.triggerMs() > positionMs && it.quizId !in answeredIds }
                    .forEach { triggeredIds.remove(it.quizId) }
                null
            }
            else -> crossedMarkers(previous, positionMs).firstOrNull()?.also { marker ->
                triggeredIds.add(marker.quizId)
                skippedIds.remove(marker.quizId)
            }
        }
    }

    fun markAnswered(quizId: String) {
        answeredIds.add(quizId)
        skippedIds.remove(quizId)
    }

    fun reset() {
        markers = emptyList()
        answeredIds.clear()
        triggeredIds.clear()
        skippedIds.clear()
        lastPositionMs = null
    }

    private fun crossedMarkers(fromExclusiveMs: Long, toInclusiveMs: Long): List<QuizMarker> =
        markers.filter { marker ->
            val triggerMs = marker.triggerMs()
            marker.quizId !in answeredIds &&
                marker.quizId !in triggeredIds &&
                triggerMs > fromExclusiveMs &&
                triggerMs <= toInclusiveMs
        }

    private fun QuizMarker.triggerMs(): Long = (timestampSec * 1000f).toLong()

    private companion object {
        const val SEEK_JUMP_THRESHOLD_MS = 1_000L
        const val BASELINE_POSITION_MS = -1L
    }
}
