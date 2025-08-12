package com.kurt.mynoteapp.data.repository

import com.kurt.mynoteapp.data.local.Note
import com.kurt.mynoteapp.data.local.NoteDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {
    override fun observeNotes(): Flow<List<Note>> = noteDao.getNotes()

    override suspend fun upsert(note: Note): Long = noteDao.upsert(note)

    override suspend fun delete(note: Note) = noteDao.delete(note)

    override suspend fun getById(id: Long): Note? = noteDao.getById(id)
}


