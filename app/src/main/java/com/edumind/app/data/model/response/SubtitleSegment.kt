package com.edumind.app.data.model.response

data class SubtitleSegment(
    val startMs: Long,
    val endMs: Long,
    val text: String
)

fun List<SubtitleSegment>.indexAt(positionMs: Long): Int {
    var low = 0
    var high = lastIndex
    while (low <= high) {
        val mid = (low + high) ushr 1
        val segment = this[mid]
        when {
            positionMs < segment.startMs -> high = mid - 1
            positionMs >= segment.endMs -> low = mid + 1
            else -> return mid
        }
    }
    return -1
}

