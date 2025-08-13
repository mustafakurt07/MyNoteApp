package com.kurt.mynoteapp.ui.note.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurt.mynoteapp.data.local.Note
import com.kurt.mynoteapp.domain.usecase.DeleteNoteUseCase
import com.kurt.mynoteapp.domain.usecase.ObserveNotesUseCase
import com.kurt.mynoteapp.util.CommonUtil
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
import kotlinx.coroutines.flow.map
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

    private val debouncedQuery = _query
        .map { it.trim() }
        .debounce(300)
        .distinctUntilChanged()


    // UI ham query'yi anında görsün; filtreleme debounce edilmiş query ile çalışsın
    val uiState: StateFlow<NoteListUiState> = combine(
        _isLoading,
        _notes,
        _query,              // raw query -> UI
        _tagFilters,
        debouncedQuery       // debounced -> filtering
    ) { isLoading, notes, rawQuery, tagFilters, filtQuery ->
        val filtered = notes.filter { note ->
            val matchesQuery = filtQuery.isBlank() || 
                note.title.contains(filtQuery, true) || 
                note.content.contains(filtQuery, true)
            val matchesTags = tagFilters.isEmpty() || note.tags.any { it in tagFilters }
            matchesQuery && matchesTags
        }
        val allTags = notes.flatMap { it.tags }.toSet()
        NoteListUiState(
            isLoading = isLoading,
            notes = notes,
            query = rawQuery,          // UI anında güncellenir
            tagFilters = tagFilters,
            filteredNotes = filtered,
            allTags = allTags
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
            is NoteListIntent.ClearFilters -> {
                _query.value = CommonUtil.emptyString()
                _tagFilters.value = emptySet()
            }
        }
    }
}


