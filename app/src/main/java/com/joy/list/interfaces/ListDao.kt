package com.joy.list.interfaces

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import com.joy.list.utils.List

/**
 * Runs queries pertaining to lists.
 */
@Dao
interface ListDao {
    /**
     * Inserts a list into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List)

    /**
     * Updates a list in the database.
     */
    @Update
    suspend fun update(list: List)

    /**
     * Deletes a list from the database.
     */
    @Delete
    suspend fun delete(list: List)

    /**
     * Gets a list with a particular id.
     */
    @Query("SELECT * FROM ${List.TABLE_NAME} WHERE id = :id")
    suspend fun get(id: Int?): List

    /**
     * Gets all lists from the database.
     */
    @Query("SELECT * FROM ${List.TABLE_NAME}")
    fun getAll(): LiveData<kotlin.collections.List<List>>
}