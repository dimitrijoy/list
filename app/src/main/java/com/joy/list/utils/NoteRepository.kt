package com.joy.list.utils

import android.app.Application
import androidx.lifecycle.LiveData

import com.joy.list.interfaces.NoteDao
import kotlin.collections.List

/**
 * Provides a layer of abstraction for NoteDao.
 */
class NoteRepository(application: Application) {
    private val noteDao: NoteDao

    /**
     * Initializes an instance of NoteDao.
     */
    init {
        val appDatabase: AppDatabase = AppDatabase.getInstance(application)
        noteDao = appDatabase.noteDao()
    }
    
    /**
     * Gets all notes.
     */
    fun getAll(): LiveData<List<Note>> {
        return noteDao.getAll()
    }
    
    /**
     * Passes a note to be inserted by noteDao.
     */
    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    /**
     * Passes a note to be updated by noteDao.
     */
    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    /**
     * Passes a note to be deleted by noteDao.
     */
    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }
}