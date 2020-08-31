package com.joy.list.interfaces

import androidx.lifecycle.LiveData
import androidx.room.*

import com.joy.list.utils.Item

/**
 * Runs queries pertaining to items.
 */
@Dao
interface ItemDao {
    /**
     * Inserts an item into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    /**
     * Updates an item in the database.
     */
    @Update
    suspend fun update(item: Item)

    /**
     * Deletes an item from the database.
     */
    @Delete
    suspend fun delete(item: Item)

    /**
     * Deletes all item from a particular list in the database.
     */
    @Query("DELETE FROM ${Item.TABLE_NAME} WHERE list_id = :listId")
    suspend fun deleteAll(listId: Int?)

    /**
     * Gets all items of a particular list from the database.
     */
    @Query("SELECT * FROM ${Item.TABLE_NAME} WHERE list_id = :listId ORDER BY id DESC")
    fun getAll(listId: Int?): LiveData<List<Item>>

    /**
     * Gets all items of a particular list and with a particular completed status from the database.
     */
    @Query("SELECT * FROM ${Item.TABLE_NAME} WHERE list_id = :listId AND complete = :complete ORDER BY id DESC")
    fun getAll(listId: Int?, complete: Boolean?): LiveData<List<Item>>

    /**
     * Gets all items of a particular list by alphabetical order from the database.
     */
    @Query("SELECT * FROM ${Item.TABLE_NAME} WHERE list_id = :listId ORDER BY title")
    fun getAllByAlphabeticalOrder(listId: Int?): LiveData<List<Item>>

    /**
     * Gets all items of a particular list by due date from the database.
     */
    @Query("SELECT * FROM ${Item.TABLE_NAME} WHERE list_id = :listId ORDER BY due_date_in_millis")
    fun getAllByDueDate(listId: Int?): LiveData<List<Item>>

    /**
     * Gets all items of a particular list by alphabetical order and with a particular completed status from the database.
     */
    @Query("SELECT * FROM ${Item.TABLE_NAME} WHERE list_id = :listId AND complete = :complete ORDER BY title")
    fun getAllByAlphabeticalOrder(listId: Int?, complete: Boolean?): LiveData<List<Item>>

    /**
     * Gets all items by due date and with a particular completed status from the database.
     */
    @Query("SELECT * FROM ${Item.TABLE_NAME} WHERE complete = :complete AND due_date_in_millis != 12345678987654321 ORDER BY due_date_in_millis")
    fun getAllByDueDate(complete: Boolean?): LiveData<List<Item>>

    /**
     * Gets all items of a particular list by due date and with a particular completed status from the database.
     */
    @Query("SELECT * FROM ${Item.TABLE_NAME} WHERE list_id = :listId AND complete = :complete ORDER BY due_date_in_millis")
    fun getAllByDueDate(listId: Int?, complete: Boolean?): LiveData<List<Item>>

    /**
     * Gets all items with a particular completed/favorited status from the database.
     */
    @Query("SELECT * FROM ${Item.TABLE_NAME} WHERE complete = :complete AND favorite = :favorite ORDER BY id DESC")
    fun getAll(complete: Boolean?, favorite: Boolean?): LiveData<List<Item>>

    /**
     * Gets all items that contain a particular string from the database; e.g., an item that
     * contains the string in its title will return.
     */
    @Query("SELECT * FROM items WHERE title LIKE :title")
    fun getAllThatContain(title: String?): LiveData<List<Item>>
}