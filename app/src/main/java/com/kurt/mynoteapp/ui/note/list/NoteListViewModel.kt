package com.kurt.mynoteapp.ui.note.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurt.mynoteapp.data.local.Note
import com.kurt.mynoteapp.domain.usecase.DeleteNoteUseCase
import com.kurt.mynoteapp.domain.usecase.ObserveNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteListUiState(
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val query: String = "",
    val tagFilters: Set<String> = emptySet(),
    val snackbar: String? = null
)

sealed interface NoteListIntent {
    data class Delete(val note: Note) : NoteListIntent
    data class ChangeQuery(val value: String) : NoteListIntent
    data class ToggleTag(val value: String) : NoteListIntent
    data class SnackbarShown(val consumed: Boolean = true) : NoteListIntent
}

@HiltViewModel
class NoteListViewModel @Inject constructor(
    observeNotes: ObserveNotesUseCase,
    private val deleteNote: DeleteNoteUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(NoteListUiState())
    val uiState: StateFlow<NoteListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeNotes().collect { list ->
                _uiState.update { it.copy(isLoading = false, notes = list) }
            }
        }
    }

    fun onIntent(intent: NoteListIntent) {
        when (intent) {
            is NoteListIntent.Delete -> viewModelScope.launch { deleteNote(intent.note) }
            is NoteListIntent.ChangeQuery -> _uiState.update { it.copy(query = intent.value) }
            is NoteListIntent.ToggleTag -> _uiState.update { st ->
                val new = st.tagFilters.toMutableSet().apply { if (!add(intent.value)) remove(intent.value) }
                st.copy(tagFilters = new)
            }
            is NoteListIntent.SnackbarShown -> _uiState.update { it.copy(snackbar = null) }
        }
    }
}


