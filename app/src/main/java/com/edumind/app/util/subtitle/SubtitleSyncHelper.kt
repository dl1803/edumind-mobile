package com.edumind.app.util.subtitle

import com.edumind.app.data.model.response.SubtitleSegment

object SubtitleSyncHelper {

    fun findCueIndex(cues: List<SubtitleSegment>, positionMs: Long): Int {
        val index = lastIndexStartingAtOrBefore(cues, positionMs)
        return if (index >= 0 && positionMs < cues[index].endMs) index else -1
    }

    fun findNearestIndex(cues: List<SubtitleSegment>, positionMs: Long): Int {
        if (cues.isEmpty()) return -1
        val index = lastIndexStartingAtOrBefore(cues, positionMs)
        if (index < 0) return 0
        if (positionMs < cues[index].endMs) return index
        val next = index + 1
        if (next > cues.lastIndex) return index
        val distanceToPrevious = positionMs - cues[index].endMs
        val distanceToNext = cues[next].startMs - positionMs
        return if (distanceToNext <= distanceToPrevious) next else index
    }

    private fun lastIndexStartingAtOrBefore(cues: List<SubtitleSegment>, positionMs: Long): Int {
        var low = 0
        var high = cues.lastIndex
        var result = -1
        while (low <= high) {
            val mid = (low + high) ushr 1
            if (cues[mid].startMs <= positionMs) {
                result = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return result
    }
}

