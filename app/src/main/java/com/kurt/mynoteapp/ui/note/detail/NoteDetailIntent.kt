package com.kurt.mynoteapp.ui.note.detail

sealed interface NoteDetailIntent {
    data class Load(val id: Long) : NoteDetailIntent
    data class ChangeTitle(val title: String) : NoteDetailIntent
    data class ChangeContent(val content: String) : NoteDetailIntent
    data class ChangeTagInput(val value: String) : NoteDetailIntent
    data class AddTag(val tag: String) : NoteDetailIntent
    data class RemoveTag(val tag: String) : NoteDetailIntent
    data object Save : NoteDetailIntent
}