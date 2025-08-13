package com.kurt.mynoteapp.ui.note.detail

sealed interface NoteDetailEvent {
    data object Close : NoteDetailEvent
}