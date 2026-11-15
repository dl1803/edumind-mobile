package com.edumind.app.util.subtitle

import com.edumind.app.data.model.response.SubtitleSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureNanoTime

class SubtitleSyncHelperTest {

    private fun cue(startMs: Long, endMs: Long) =
        SubtitleSegment(startMs = startMs, endMs = endMs, text = "cue")

    private val cues = listOf(
        cue(1_000L, 3_000L),
        cue(5_000L, 8_000L),
        cue(8_000L, 10_000L)
    )

    @Test
    fun findCueIndex_emptyList_returnsMinusOne() {
        assertEquals(-1, SubtitleSyncHelper.findCueIndex(emptyList(), 0L))
        assertEquals(-1, SubtitleSyncHelper.findCueIndex(emptyList(), 5_000L))
    }

    @Test
    fun findCueIndex_positionBeforeFirstCue_returnsMinusOne() {
        assertEquals(-1, SubtitleSyncHelper.findCueIndex(cues, 0L))
        assertEquals(-1, SubtitleSyncHelper.findCueIndex(cues, 999L))
    }

    @Test
    fun findCueIndex_positionEqualsStartMs_returnsThatCue() {
        assertEquals(0, SubtitleSyncHelper.findCueIndex(cues, 1_000L))
        assertEquals(1, SubtitleSyncHelper.findCueIndex(cues, 5_000L))
    }

    @Test
    fun findCueIndex_positionInsideCue_returnsThatCue() {
        assertEquals(0, SubtitleSyncHelper.findCueIndex(cues, 2_000L))
        assertEquals(1, SubtitleSyncHelper.findCueIndex(cues, 6_500L))
        assertEquals(2, SubtitleSyncHelper.findCueIndex(cues, 9_000L))
    }

    @Test
    fun findCueIndex_positionEqualsEndMsWithGapAfter_returnsMinusOne() {
        assertEquals(-1, SubtitleSyncHelper.findCueIndex(cues, 3_000L))
    }

    @Test
    fun findCueIndex_positionEqualsEndMsWhenNextCueStartsAtSameMoment_returnsNextCue() {
        assertEquals(2, SubtitleSyncHelper.findCueIndex(cues, 8_000L))
    }

    @Test
    fun findCueIndex_positionInGapBetweenCues_returnsMinusOne() {
        assertEquals(-1, SubtitleSyncHelper.findCueIndex(cues, 3_001L))
        assertEquals(-1, SubtitleSyncHelper.findCueIndex(cues, 4_000L))
        assertEquals(-1, SubtitleSyncHelper.findCueIndex(cues, 4_999L))
    }

    @Test
    fun findCueIndex_positionAfterLastCue_returnsMinusOne() {
        assertEquals(-1, SubtitleSyncHelper.findCueIndex(cues, 10_000L))
        assertEquals(-1, SubtitleSyncHelper.findCueIndex(cues, 20_000L))
    }

    @Test
    fun findCueIndex_tenThousandCues_returnsCorrectIndexQuickly() {
        val largeList = List(10_000) { i -> cue(i * 1_000L, i * 1_000L + 800L) }

        assertEquals(0, SubtitleSyncHelper.findCueIndex(largeList, 0L))
        assertEquals(5_000, SubtitleSyncHelper.findCueIndex(largeList, 5_000_400L))
        assertEquals(-1, SubtitleSyncHelper.findCueIndex(largeList, 5_000_800L))
        assertEquals(9_999, SubtitleSyncHelper.findCueIndex(largeList, 9_999_400L))
        assertEquals(-1, SubtitleSyncHelper.findCueIndex(largeList, 9_999_800L))

        val elapsedNs = measureNanoTime {
            repeat(100) {
                SubtitleSyncHelper.findCueIndex(largeList, 9_999_400L)
            }
        }
        assertTrue("100 lookups took ${elapsedNs / 1_000_000.0}ms", elapsedNs < 10_000_000L)
    }
}
