package com.kurt.mynoteapp.data.repository

import com.kurt.mynoteapp.data.local.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
    suspend fun upsert(note: Note): Long
    suspend fun delete(note: Note)
    suspend fun getById(id: Long): Note?
}


