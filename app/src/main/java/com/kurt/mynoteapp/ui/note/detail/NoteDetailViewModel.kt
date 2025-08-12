package com.kurt.mynoteapp.ui.note.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurt.mynoteapp.data.local.Note
import com.kurt.mynoteapp.domain.usecase.GetNoteByIdUseCase
import com.kurt.mynoteapp.domain.usecase.UpsertNoteUseCase
import com.kurt.mynoteapp.util.CommonUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val getNoteById: GetNoteByIdUseCase,
    private val upsertNote: UpsertNoteUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    fun onIntent(intent: NoteDetailIntent) {
        when (intent) {
            is NoteDetailIntent.Load -> load(intent.id)
            is NoteDetailIntent.ChangeTitle -> _uiState.update { it.copy(note = it.note.copy(title = intent.title)) }
            is NoteDetailIntent.ChangeContent -> _uiState.update { it.copy(note = it.note.copy(content = intent.content)) }
            is NoteDetailIntent.ChangeTagInput -> _uiState.update { it.copy(tagInput = intent.value) }
            is NoteDetailIntent.AddTag -> addTag(intent.tag)
            is NoteDetailIntent.RemoveTag -> removeTag(intent.tag)
            NoteDetailIntent.Save -> save()
        }
    }

    private fun load(id: Long) {
        viewModelScope.launch {
            val loaded = if (id == 0L) Note(title = CommonUtil.emptyString(), content = CommonUtil.emptyString()) else getNoteById(id) ?: Note(title = CommonUtil.emptyString(), content = CommonUtil.emptyString())
            _uiState.update { it.copy(isLoading = false, note = loaded) }
        }
    }

    private fun save() {
        viewModelScope.launch { upsertNote(uiState.value.note) }
    }

    private fun addTag(raw: String) {
        val tag = raw.trim()
        if (tag.isEmpty()) return
        _uiState.update { state ->
            if (state.note.tags.contains(tag)) state
            else state.copy(
                note = state.note.copy(tags = state.note.tags + tag),
                tagInput = CommonUtil.emptyString()
            )
        }
    }

    private fun removeTag(tag: String) {
        _uiState.update { state ->
            state.copy(note = state.note.copy(tags = state.note.tags.filterNot { it == tag }))
        }
    }
}


