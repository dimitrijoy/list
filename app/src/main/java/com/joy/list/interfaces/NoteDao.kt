package com.joy.list.interfaces

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import com.joy.list.utils.Note

/**
 * Runs queries pertaining to notes.
 */
@Dao
interface NoteDao {
    /**
     * Inserts a note into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    /**
     * Updates a note in the database.
     */
    @Update
    suspend fun update(note: Note)

    /**
     * Deletes a note from the database.
     */
    @Delete
    suspend fun delete(note: Note)

    /**
     * Gets all notes.
     */
    @Query("SELECT * FROM ${Note.TABLE_NAME} ORDER BY id DESC")
    fun getAll(): LiveData<List<Note>>
}