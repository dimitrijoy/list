package com.joy.list.utils

import android.app.Application
import androidx.lifecycle.LiveData

import com.joy.list.interfaces.ItemDao
import kotlin.collections.List

/**
 * Provides a layer of abstraction for ItemDao.
 */
class ItemRepository(application: Application) {
    private val itemDao: ItemDao

    /**
     * Initializes an instance of ItemDao.
     */
    init {
        val appDatabase: AppDatabase = AppDatabase.getInstance(application)
        itemDao = appDatabase.itemDao()
    }

    /**
     * Gets all items of a particular list.
     */
    fun getAll(listId: Int?): LiveData<List<Item>> {
        return itemDao.getAll(listId)
    }

    /**
     * Gets all items from a particular list with a particular sort.
     */
    fun getAll(listId: Int?, sortBy: Int): LiveData<List<Item>> {
        return when (sortBy) {
            com.joy.list.utils.List.ALPHABETICAL_ORDER -> itemDao.getAllByAlphabeticalOrder(listId)
            com.joy.list.utils.List.DUE_DATE -> itemDao.getAllByDueDate(listId)
            else -> itemDao.getAll(listId)
        }
    }

    /**
     * Gets all items from a particular list with a particular sort and completed status.
     */
    fun getAll(listId: Int?, complete: Boolean?, sortBy: Int): LiveData<List<Item>> {
        return when (sortBy) {
            com.joy.list.utils.List.ALPHABETICAL_ORDER -> itemDao.getAllByAlphabeticalOrder(listId, complete)
            com.joy.list.utils.List.DUE_DATE -> itemDao.getAllByDueDate(listId, complete)
            else -> itemDao.getAll(listId, complete)
        }
    }

    /**
     * Gets all items of a particular completed status, sorted by due date.
     */
    fun getAll(complete: Boolean?): LiveData<List<Item>> {
        return itemDao.getAllByDueDate(complete)
    }

    /**
     * Gets all items from a particular list with a particular completed/favorited status.
     */
    fun getAll(complete: Boolean?, favorite: Boolean?): LiveData<List<Item>> {
        return itemDao.getAll(complete, favorite)
    }

    /**
     * Gets all items that contain a particular string.
     */
    fun getAllThatContain(title: String?): LiveData<List<Item>> {
        return itemDao.getAllThatContain(title)
    }

    /**
     * Passes an item to be inserted by itemDao.
     */
    suspend fun insert(item: Item) {
        itemDao.insert(item)
    }

    /**
     * Passes an item to be updated by itemDao.
     */
    suspend fun update(item: Item) {
        itemDao.update(item)
    }

    /**
     * Passes an item to be deleted by itemDao.
     */
    suspend fun delete(item: Item) {
        itemDao.delete(item)
    }

    /**
     * Deletes all items from a particular list.
     */
    suspend fun deleteAll(listId: Int?) {
        itemDao.deleteAll(listId)
    }
}