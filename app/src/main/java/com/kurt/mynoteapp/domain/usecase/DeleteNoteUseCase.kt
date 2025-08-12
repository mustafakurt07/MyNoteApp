package com.kurt.mynoteapp.domain.usecase

import com.kurt.mynoteapp.data.local.Note
import com.kurt.mynoteapp.data.repository.NoteRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) = repository.delete(note)
}


