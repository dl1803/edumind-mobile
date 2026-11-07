package com.edumind.app.ui.player.notes

import androidx.lifecycle.SavedStateHandle
import com.edumind.app.data.model.response.Note
import com.edumind.app.repository.NoteRepository
import com.edumind.app.ui.navigation.Screen
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: NotesViewModel

    private val sampleLessonId = "lesson_test_101"

    private val note1 = Note(
        id = "n1",
        lessonId = sampleLessonId,
        content = "[Cú pháp] Ghi chú tại 02:00",
        timestampSeconds = 120f,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    private val note2 = Note(
        id = "n2",
        lessonId = sampleLessonId,
        content = "[Lỗi thường gặp] Ghi chú tại 00:30",
        timestampSeconds = 30f,
        createdAt = 2000L,
        updatedAt = 2000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_LESSON_ID to sampleLessonId))
        coEvery { noteRepository.getNotes(sampleLessonId) } returns flowOf(listOf(note1, note2))
        coEvery { noteRepository.fetchNotesFromRemote(sampleLessonId) } returns Result.success(Unit)

        viewModel = NotesViewModel(
            savedStateHandle = savedStateHandle,
            noteRepository = noteRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadNotes_success_sortsNotesAscendingByTimestamp() = runTest(testDispatcher) {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is NotesUiState.Success)
        val list = (state as NotesUiState.Success).notes
        assertEquals(2, list.size)
        assertEquals("n2", list[0].id)
        assertEquals("n1", list[1].id)
    }

    @Test
    fun setFilterTag_filtersNotesCorrectly() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.setFilterTag("Cú pháp")
        val state = viewModel.uiState.value
        assertTrue(state is NotesUiState.Success)
        val list = (state as NotesUiState.Success).notes
        assertEquals(1, list.size)
        assertEquals("n1", list[0].id)

        viewModel.setFilterTag("Tất cả")
        val allState = viewModel.uiState.value as NotesUiState.Success
        assertEquals(2, allState.notes.size)
    }

    @Test
    fun loadNotes_empty_emitsEmptyState() = runTest(testDispatcher) {
        coEvery { noteRepository.getNotes("lesson_empty") } returns flowOf(emptyList())
        coEvery { noteRepository.fetchNotesFromRemote("lesson_empty") } returns Result.success(Unit)

        viewModel.initLessonId("lesson_empty")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is NotesUiState.Empty)
    }

    @Test
    fun loadNotes_networkErrorWithEmptyCache_emitsErrorState() = runTest(testDispatcher) {
        coEvery { noteRepository.getNotes("lesson_err") } returns flowOf(emptyList())
        coEvery { noteRepository.fetchNotesFromRemote("lesson_err") } returns Result.failure(Exception("Lỗi kết nối"))

        viewModel.initLessonId("lesson_err")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is NotesUiState.Error)
    }

    @Test
    fun openCreateSheet_autofillsCurrentPosition() = runTest(testDispatcher) {
        viewModel.openCreateSheet(currentPositionMs = 75_000L)

        val editorState = viewModel.editorState.value
        assertTrue(editorState.isOpen)
        assertFalse(editorState.isEditing)
        assertEquals(75f, editorState.timestampSeconds, 0.01f)
        assertEquals("", editorState.content)
    }

    @Test
    fun onContentChange_enforces500CharactersLimit() = runTest(testDispatcher) {
        val text600Chars = "A".repeat(600)
        viewModel.onContentChange(text600Chars)

        val editorState = viewModel.editorState.value
        assertEquals(500, editorState.content.length)
        assertEquals(500, editorState.characterCount)
    }

    @Test
    fun saveNote_withTag_formatsContentAndClosesSheet() = runTest(testDispatcher) {
        viewModel.openCreateSheet(30_000L)
        viewModel.onSelectTag("Cú pháp")
        viewModel.onContentChange("Khai báo biến PEP 8")

        coEvery {
            noteRepository.createNote(any(), "[Cú pháp] Khai báo biến PEP 8", any())
        } returns Result.success(note1)

        viewModel.saveNote()
        advanceUntilIdle()

        assertFalse(viewModel.editorState.value.isOpen)
        coVerify(atLeast = 1) { 
            noteRepository.createNote(any(), "[Cú pháp] Khai báo biến PEP 8", any()) 
        }
    }

    @Test
    fun deleteNote_success_removesFromList() = runTest(testDispatcher) {
        advanceUntilIdle()

        coEvery { noteRepository.deleteNote("n1") } returns Result.success(Unit)

        viewModel.requestDelete(note1)
        assertEquals(note1, viewModel.noteToDelete.value)

        viewModel.confirmDelete()
        advanceUntilIdle()

        assertNull(viewModel.noteToDelete.value)
        coVerify(exactly = 1) { noteRepository.deleteNote("n1") }
    }
}

