package com.kurt.mynoteapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.runtime.Immutable

@Immutable
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList()
)


