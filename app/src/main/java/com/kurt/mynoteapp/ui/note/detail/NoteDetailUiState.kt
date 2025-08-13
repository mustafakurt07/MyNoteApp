package com.kurt.mynoteapp.ui.note.detail

import com.kurt.mynoteapp.data.local.Note
import com.kurt.mynoteapp.util.CommonUtil

data class NoteDetailUiState(
    val isLoading: Boolean = true,
    val note: Note = Note(title = CommonUtil.emptyString(), content = CommonUtil.emptyString()),
    val tagInput: String = CommonUtil.emptyString(),
    val closeRequested: Boolean = false
)
