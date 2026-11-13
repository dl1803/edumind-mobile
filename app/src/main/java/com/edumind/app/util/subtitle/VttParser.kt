package com.edumind.app.util.subtitle

import com.edumind.app.data.model.response.SubtitleSegment

object VttParser {

    private val TIMING_LINE = Regex(
        """^\s*(?:(\d{2,}):)?(\d{2}):(\d{2})\.(\d{3})\s+-->\s+(?:(\d{2,}):)?(\d{2}):(\d{2})\.(\d{3})(?:\s.*)?$"""
    )
    private val MARKUP_TAG = Regex("""</?[a-zA-Z][^>]*>""")

    fun parse(content: String): List<SubtitleSegment> {
        val normalized = content
            .removePrefix("\uFEFF")
            .replace("\r\n", "\n")
            .replace('\r', '\n')
        return splitBlocks(normalized)
            .mapNotNull { parseBlock(it) }
            .sortedBy { it.startMs }
    }

    private fun splitBlocks(content: String): List<List<String>> {
        val blocks = mutableListOf<List<String>>()
        var current = mutableListOf<String>()
        for (line in content.split('\n')) {
            if (line.isBlank()) {
                if (current.isNotEmpty()) {
                    blocks.add(current)
                    current = mutableListOf()
                }
            } else {
                current.add(line)
            }
        }
        if (current.isNotEmpty()) blocks.add(current)
        return blocks
    }

    private fun parseBlock(lines: List<String>): SubtitleSegment? {
        val firstLine = lines.first()
        if (firstLine.startsWith("WEBVTT") || isNoteBlock(firstLine)) return null
        val timingIndex = lines.indexOfFirst { it.contains("-->") }
        if (timingIndex < 0) return null
        val match = TIMING_LINE.matchEntire(lines[timingIndex]) ?: return null
        val values = match.groupValues
        val startMs = toMillis(values[1], values[2], values[3], values[4])
        val endMs = toMillis(values[5], values[6], values[7], values[8])
        if (endMs <= startMs) return null
        val text = lines
            .drop(timingIndex + 1)
            .map { MARKUP_TAG.replace(it, "").trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
        if (text.isEmpty()) return null
        return SubtitleSegment(startMs = startMs, endMs = endMs, text = text)
    }

    private fun isNoteBlock(firstLine: String): Boolean =
        firstLine == "NOTE" || firstLine.startsWith("NOTE ") || firstLine.startsWith("NOTE\t")

    private fun toMillis(hours: String, minutes: String, seconds: String, millis: String): Long =
        (hours.toLongOrNull() ?: 0L) * 3_600_000L +
            minutes.toLong() * 60_000L +
            seconds.toLong() * 1_000L +
            millis.toLong()
}

