package com.kurt.mynoteapp.ui.note.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurt.mynoteapp.data.local.Note
import com.kurt.mynoteapp.domain.usecase.DeleteNoteUseCase
import com.kurt.mynoteapp.domain.usecase.ObserveNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class NoteListViewModel @Inject constructor(
    observeNotes: ObserveNotesUseCase,
    private val deleteNote: DeleteNoteUseCase
) : ViewModel() {
    
    // Ayrı MutableStateFlow'lar - döngü riski yok
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    private val _query = MutableStateFlow("")
    private val _tagFilters = MutableStateFlow<Set<String>>(emptySet())
    private val _isLoading = MutableStateFlow(true)
    
    // Reaktif filtreleme: notes + query + tagFilters değişince otomatik filtreleme
    val uiState: StateFlow<NoteListUiState> = combine(
        _isLoading,
        _notes,
        _query.debounce(300).distinctUntilChanged(), // Sadece query'de debounce
        _tagFilters
    ) { isLoading, notes, query, tagFilters ->
        val filtered = notes.filter { note ->
            val matchesQuery = query.isBlank() || 
                note.title.contains(query, true) || 
                note.content.contains(query, true)
            
            val matchesTags = tagFilters.isEmpty() || 
                note.tags.any { tag -> tag in tagFilters }
            
            matchesQuery && matchesTags
        }
        
        NoteListUiState(
            isLoading = isLoading,
            notes = notes,
            query = query,
            tagFilters = tagFilters,
            filteredNotes = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = NoteListUiState(),
        started = SharingStarted.WhileSubscribed()
    )

    init {
        viewModelScope.launch {
            observeNotes().collect { list ->
                _notes.value = list
                _isLoading.value = false
            }
        }
    }

    fun onIntent(intent: NoteListIntent) {
        when (intent) {
            is NoteListIntent.Delete -> viewModelScope.launch { deleteNote(intent.note) }
            is NoteListIntent.ChangeQuery -> _query.value = intent.value
            is NoteListIntent.ToggleTag -> {
                val current = _tagFilters.value.toMutableSet()
                if (!current.add(intent.value)) current.remove(intent.value)
                _tagFilters.value = current
            }
        }
    }
}


