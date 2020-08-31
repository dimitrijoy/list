package com.joy.list.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

import com.joy.list.utils.Note
import com.joy.list.utils.NoteRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Directly interacts with app activities and provides a layer of abstraction for NoteRepository.
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val noteRepository: NoteRepository = NoteRepository(application)
    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Launches a coroutine for inserting a note.
     */
    fun insertNote(note: Note) {
        viewModelScope.launch {
            insert(note)
        }
    }

    /**
     * Passes a note to be inserted by noteRepository.
     */
    private suspend fun insert(note: Note) {
        noteRepository.insert(note)
    }

    /**
     * Launches a coroutine for updating a note.
     */
    fun updateNote(note: Note) {
        viewModelScope.launch {
            update(note)
        }
    }

    /**
     * Passes a note to be updated by noteRepository.
     */
    private suspend fun update(note: Note) {
        noteRepository.update(note)
    }

    /**
     * Launches a coroutine for deleting a note.
     */
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            delete(note)
        }
    }

    /**
     * Passes a note to be deleted by noteRepository.
     */
    private suspend fun delete(note: Note) {
        noteRepository.delete(note)
    }

    /**
     * Gets all notes.
     */
    fun getAllNotes(): LiveData<List<Note>> {
        return noteRepository.getAll()
    }
}