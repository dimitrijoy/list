package com.joy.list.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

import com.joy.list.utils.Item
import com.joy.list.utils.ItemRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Directly interacts with app activities and provides a layer of abstraction for ItemRepository.
 */
class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val itemRepository: ItemRepository = ItemRepository(application)
    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Launches a coroutine for inserting an item.
     */
    fun insertItem(item: Item) {
        viewModelScope.launch {
            insert(item)
        }
    }

    /**
     * Passes an item to be inserted by itemRepository.
     */
    private suspend fun insert(item: Item) {
        itemRepository.insert(item)
    }

    /**
     * Launches a coroutine for updating an item.
     */
    fun updateItem(item: Item) {
        viewModelScope.launch {
            update(item)
        }
    }

    /**
     * Passes an item to be updated by itemRepository.
     */
    private suspend fun update(item: Item) {
        itemRepository.update(item)
    }

    /**
     * Launches a coroutine for deleting an item.
     */
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            delete(item)
        }
    }

    /**
     * Passes an item to be deleted by itemRepository.
     */
    private suspend fun delete(item: Item) {
        itemRepository.delete(item)
    }

    /**
     * Launches a coroutine for deleting all items from a particular list.
     */
    fun deleteAllItemsFrom(listId: Int?) {
        viewModelScope.launch {
            deleteAll(listId)
        }
    }

    /**
     * Deletes all items from a particular list.
     */
    private suspend fun deleteAll(listId: Int?) {
        itemRepository.deleteAll(listId)
    }

    /**
     * Gets all items of a particular list.
     */
    fun getAllItemsFrom(listId: Int?): LiveData<List<Item>> {
        return itemRepository.getAll(listId)
    }

    /**
     * Gets all items of a particular list with a particular sort.
     */
    fun getAllItemsFrom(listId: Int?, sortBy: Int): LiveData<List<Item>> {
        return itemRepository.getAll(listId, sortBy)
    }

    /**
     * Gets all items from a particular list with a particular sort and completed status.
     */
    fun getAllItemsFrom(listId: Int?, complete: Boolean?, sortBy: Int): LiveData<List<Item>> {
        return itemRepository.getAll(listId, complete, sortBy)
    }

    /**
     * Gets all items of a particular completed status, sorted by due date.
     */
    fun getAllItemsFrom(complete: Boolean?): LiveData<List<Item>> {
        return itemRepository.getAll(complete)
    }

    /**
     * Gets all items from a particular list with a particular completed/favorited status.
     */
    fun getAllItemsFrom(complete: Boolean?, favorite: Boolean?): LiveData<List<Item>> {
        return itemRepository.getAll(complete, favorite)
    }

    /**
     * Gets all items that contain a particular string.
     */
    fun getAllItemsThatContain(title: String?): LiveData<List<Item>> {
        return itemRepository.getAllThatContain(title)
    }
}