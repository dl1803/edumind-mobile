package com.edumind.app.ui.player.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.model.response.Note
import com.edumind.app.repository.NoteRepository
import com.edumind.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NotesUiState {
    data object Loading : NotesUiState
    data class Success(val notes: List<Note>) : NotesUiState
    data object Empty : NotesUiState
    data class Error(val message: String) : NotesUiState
}

data class NoteEditorUiState(
    val isOpen: Boolean = false,
    val isEditing: Boolean = false,
    val noteId: String? = null,
    val lessonId: String = "",
    val timestampSeconds: Float = 0f,
    val content: String = "",
    val selectedTag: String? = null,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = content.trim().isNotEmpty() && !isSaving

    val characterCount: Int
        get() = content.length

    companion object {
        const val MAX_CONTENT_LENGTH = 500
    }
}

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotesUiState>(NotesUiState.Loading)
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _editorState = MutableStateFlow(NoteEditorUiState())
    val editorState: StateFlow<NoteEditorUiState> = _editorState.asStateFlow()

    private val _noteToDelete = MutableStateFlow<Note?>(null)
    val noteToDelete: StateFlow<Note?> = _noteToDelete.asStateFlow()

    private val _selectedFilterTag = MutableStateFlow("Tất cả")
    val selectedFilterTag: StateFlow<String> = _selectedFilterTag.asStateFlow()

    private var allNotes: List<Note> = emptyList()
    private var notesJob: Job? = null

    var currentLessonId: String = ""
        private set

    init {
        val navLessonId = savedStateHandle.get<String>(Screen.ARG_LESSON_ID) ?: ""
        if (navLessonId.isNotBlank()) {
            currentLessonId = navLessonId
            loadNotes(navLessonId)
        }
    }

    fun initLessonId(lessonId: String) {
        if (lessonId.isNotBlank() && lessonId != currentLessonId) {
            currentLessonId = lessonId
            loadNotes(lessonId)
        }
    }

    fun setFilterTag(tag: String) {
        _selectedFilterTag.value = tag
        applyFilter()
    }

    private fun applyFilter() {
        if (allNotes.isEmpty()) {
            if (_uiState.value !is NotesUiState.Loading && _uiState.value !is NotesUiState.Error) {
                _uiState.value = NotesUiState.Empty
            }
            return
        }

        val currentFilter = _selectedFilterTag.value
        val filtered = if (currentFilter == "Tất cả") {
            allNotes
        } else {
            allNotes.filter { it.tagLabel == currentFilter }
        }

        if (filtered.isEmpty() && currentFilter != "Tất cả") {
            _uiState.value = NotesUiState.Success(emptyList())
        } else if (filtered.isEmpty()) {
            _uiState.value = NotesUiState.Empty
        } else {
            _uiState.value = NotesUiState.Success(filtered)
        }
    }

    fun loadNotes(lessonId: String = currentLessonId) {
        if (lessonId.isBlank()) return

        notesJob?.cancel()
        allNotes = emptyList()
        notesJob = viewModelScope.launch {
            _uiState.value = NotesUiState.Loading

            launch {
                noteRepository.getNotes(lessonId)
                    .catch { e ->
                        _uiState.value = NotesUiState.Error(e.message ?: "Lỗi khi đọc dữ liệu ghi chú.")
                    }
                    .collect { notes ->
                        allNotes = notes.sortedBy { it.timestampSeconds }
                        if (allNotes.isNotEmpty() || _uiState.value !is NotesUiState.Loading) {
                            applyFilter()
                        }
                    }
            }

            val remoteResult = noteRepository.fetchNotesFromRemote(lessonId)
            remoteResult.fold(
                onSuccess = { remoteNotes ->
                    allNotes = remoteNotes.sortedBy { it.timestampSeconds }
                    if (allNotes.isEmpty()) {
                        _uiState.value = NotesUiState.Empty
                    } else {
                        applyFilter()
                    }
                },
                onFailure = { error ->
                    if (allNotes.isEmpty()) {
                        if (error.message?.contains("không tồn tại") == true) {
                            _uiState.value = NotesUiState.Empty
                        } else {
                            _uiState.value = NotesUiState.Error(error.message ?: "Không thể kết nối đến máy chủ.")
                        }
                    }
                }
            )
        }
    }

    fun openCreateSheet(currentPositionMs: Long) {
        val seconds = (currentPositionMs.coerceAtLeast(0L) / 1000f)
        _editorState.value = NoteEditorUiState(
            isOpen = true,
            isEditing = false,
            noteId = null,
            lessonId = currentLessonId,
            timestampSeconds = seconds,
            content = "",
            selectedTag = null,
            isSaving = false,
            error = null
        )
    }

    fun openEditSheet(note: Note) {
        _editorState.value = NoteEditorUiState(
            isOpen = true,
            isEditing = true,
            noteId = note.id,
            lessonId = note.lessonId.ifBlank { currentLessonId },
            timestampSeconds = note.timestampSeconds,
            content = note.cleanContent,
            selectedTag = note.tagLabel,
            isSaving = false,
            error = null
        )
    }

    fun closeEditorSheet() {
        _editorState.update { it.copy(isOpen = false, error = null) }
    }

    fun onContentChange(newContent: String) {
        val trimmedContent = if (newContent.length > NoteEditorUiState.MAX_CONTENT_LENGTH) {
            newContent.take(NoteEditorUiState.MAX_CONTENT_LENGTH)
        } else {
            newContent
        }
        _editorState.update { it.copy(content = trimmedContent, error = null) }
    }

    fun onSelectTag(tag: String) {
        _editorState.update {
            val newTag = if (it.selectedTag == tag) null else tag
            it.copy(selectedTag = newTag)
        }
    }

    fun updateTimestamp(newTimestampSeconds: Float) {
        _editorState.update { it.copy(timestampSeconds = newTimestampSeconds.coerceAtLeast(0f)) }
    }

    fun saveNote(onSuccess: (() -> Unit)? = null) {
        val state = _editorState.value
        if (!state.isValid) return

        val fullContent = if (!state.selectedTag.isNullOrBlank()) {
            "[${state.selectedTag}] ${state.content.trim()}"
        } else {
            state.content.trim()
        }

        viewModelScope.launch {
            _editorState.update { it.copy(isSaving = true, error = null) }

            val result = if (state.isEditing && !state.noteId.isNullOrBlank()) {
                noteRepository.updateNote(
                    noteId = state.noteId,
                    content = fullContent,
                    timestampSeconds = state.timestampSeconds
                )
            } else {
                noteRepository.createNote(
                    lessonId = state.lessonId.ifBlank { currentLessonId },
                    content = fullContent,
                    timestampSeconds = state.timestampSeconds
                )
            }

            result.fold(
                onSuccess = {
                    _editorState.update { it.copy(isOpen = false, isSaving = false, error = null) }
                    onSuccess?.invoke()
                },
                onFailure = { error ->
                    _editorState.update {
                        it.copy(
                            isSaving = false,
                            error = error.message ?: "Không thể lưu ghi chú. Vui lòng thử lại."
                        )
                    }
                }
            )
        }
    }

    fun requestDelete(note: Note) {
        _noteToDelete.value = note
    }

    fun cancelDelete() {
        _noteToDelete.value = null
    }

    fun confirmDelete(onSuccess: (() -> Unit)? = null) {
        val note = _noteToDelete.value ?: return
        viewModelScope.launch {
            val result = noteRepository.deleteNote(note.id)
            result.fold(
                onSuccess = {
                    _noteToDelete.value = null
                    if (_editorState.value.isOpen && _editorState.value.noteId == note.id) {
                        closeEditorSheet()
                    }
                    onSuccess?.invoke()
                },
                onFailure = { error ->
                    _noteToDelete.value = null
                    _editorState.update {
                        it.copy(error = error.message ?: "Không thể xóa ghi chú.")
                    }
                }
            )
        }
    }
}

