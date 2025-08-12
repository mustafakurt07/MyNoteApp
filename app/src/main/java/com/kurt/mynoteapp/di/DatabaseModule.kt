package com.kurt.mynoteapp.di

import android.content.Context
import androidx.room.Room
import com.kurt.mynoteapp.data.local.NoteDao
import com.kurt.mynoteapp.data.local.NoteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): NoteDatabase = Room.databaseBuilder(
        context,
        NoteDatabase::class.java,
        "notes.db"
    ).fallbackToDestructiveMigration(false).build()

    @Provides
    fun provideNoteDao(db: NoteDatabase): NoteDao = db.noteDao()
}


