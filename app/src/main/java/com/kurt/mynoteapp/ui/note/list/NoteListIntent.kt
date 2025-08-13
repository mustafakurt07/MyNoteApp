package com.kurt.mynoteapp.ui.note.list

import com.kurt.mynoteapp.data.local.Note

sealed interface NoteListIntent {
    data class Delete(val note: Note) : NoteListIntent
    data class ChangeQuery(val value: String) : NoteListIntent
    data class ToggleTag(val value: String) : NoteListIntent
}