package com.kurt.mynoteapp.ui.note.list

import com.kurt.mynoteapp.data.local.Note
import com.kurt.mynoteapp.util.CommonUtil

data class NoteListUiState(
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val query: String = CommonUtil.emptyString(),
    val tagFilters: Set<String> = emptySet(),
    val filteredNotes: List<Note> = emptyList(),
    val allTags: Set<String> = emptySet()
)
