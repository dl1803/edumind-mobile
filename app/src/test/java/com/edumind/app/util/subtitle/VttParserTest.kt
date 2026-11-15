package com.edumind.app.util.subtitle

import com.edumind.app.data.model.response.SubtitleSegment
import org.junit.Assert.assertEquals
import org.junit.Test

class VttParserTest {

    @Test
    fun parse_standardFile_returnsCuesWithoutHeaderNoteAndIdentifier() {
        val content = """
            WEBVTT

            NOTE Ghi chú không phải cue

            intro
            00:00:01.000 --> 00:00:03.500
            Xin chào các bạn

            2
            00:00:04.000 --> 00:00:06.250
            Chúng ta bắt đầu bài học
        """.trimIndent()

        val result = VttParser.parse(content)

        assertEquals(
            listOf(
                SubtitleSegment(startMs = 1_000L, endMs = 3_500L, text = "Xin chào các bạn"),
                SubtitleSegment(startMs = 4_000L, endMs = 6_250L, text = "Chúng ta bắt đầu bài học")
            ),
            result
        )
    }

    @Test
    fun parse_minuteSecondTimestamp_parsesMilliseconds() {
        val content = """
            WEBVTT

            00:01.500 --> 00:03.250
            Cue ngắn
        """.trimIndent()

        val result = VttParser.parse(content)

        assertEquals(
            listOf(SubtitleSegment(startMs = 1_500L, endMs = 3_250L, text = "Cue ngắn")),
            result
        )
    }

    @Test
    fun parse_multiLineCue_joinsLinesWithNewline() {
        val content = """
            WEBVTT

            00:00:01.000 --> 00:00:04.000
            Dòng thứ nhất
            Dòng thứ hai
        """.trimIndent()

        val result = VttParser.parse(content)

        assertEquals(1, result.size)
        assertEquals("Dòng thứ nhất\nDòng thứ hai", result[0].text)
    }

    @Test
    fun parse_cueWithVoiceAndClassTags_stripsTagsKeepsText() {
        val content = """
            WEBVTT

            00:00:01.000 --> 00:00:04.000
            <v Giảng viên>Thẻ <c.highlight>voice</c> được loại bỏ</v>
        """.trimIndent()

        val result = VttParser.parse(content)

        assertEquals(1, result.size)
        assertEquals("Thẻ voice được loại bỏ", result[0].text)
    }

    @Test
    fun parse_malformedCue_skipsItAndKeepsRemainingCues() {
        val content = """
            WEBVTT

            00:00:01.000 --> 00:00:02.000
            Cue đầu hợp lệ

            00:xx:03.000 --> 00:00:04.000
            Cue sai định dạng timestamp

            00:00:05.000 --> 00:00:06.000
            Cue hợp lệ sau cue lỗi
        """.trimIndent()

        val result = VttParser.parse(content)

        assertEquals(
            listOf(
                SubtitleSegment(startMs = 1_000L, endMs = 2_000L, text = "Cue đầu hợp lệ"),
                SubtitleSegment(startMs = 5_000L, endMs = 6_000L, text = "Cue hợp lệ sau cue lỗi")
            ),
            result
        )
    }

    @Test
    fun parse_vietnameseDiacritics_preservesText() {
        val text = "Tiếng Việt có dấu: ắ ằ ẳ ẵ ặ ơ ư đ Đ"
        val content = "WEBVTT\n\n00:00:01.000 --> 00:00:03.000\n$text"

        val result = VttParser.parse(content)

        assertEquals(1, result.size)
        assertEquals(text, result[0].text)
    }
}
