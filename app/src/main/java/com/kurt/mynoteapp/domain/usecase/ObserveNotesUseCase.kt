package com.kurt.mynoteapp.domain.usecase

import com.kurt.mynoteapp.data.local.Note
import com.kurt.mynoteapp.data.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> = repository.observeNotes()
}


