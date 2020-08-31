package com.joy.list.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

import com.joy.list.utils.List
import com.joy.list.utils.ListRepository
import kotlinx.coroutines.*

/**
 * Directly interacts with app activities and provides a layer of abstraction for ListRepository.
 */
class ListViewModel(application: Application) : AndroidViewModel(application) {
    private val listRepository: ListRepository = ListRepository(application)
    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Launches a coroutine for inserting a list.
     */
    fun insertList(list: List) {
        viewModelScope.launch {
            insert(list)
        }
    }

    /**
     * Passes a list to be inserted by listRepository.
     */
    private suspend fun insert(list: List) {
        listRepository.insert(list)
    }

    /**
     * Launches a coroutine for updating a list.
     */
    fun updateList(list: List) {
        viewModelScope.launch {
            update(list)
        }
    }

    /**
     * Passes a list to be updated by listRepository.
     */
    private suspend fun update(list: List) {
        listRepository.update(list)
    }

    /**
     * Launches a coroutine for deleting a list.
     */
    fun deleteList(list: List) {
        viewModelScope.launch {
            delete(list)
        }
    }

    /**
     * Passes a list to be deleted by listRepository.
     */
    private suspend fun delete(list: List) {
        listRepository.delete(list)
    }

    /**
     * Gets a list with a particular id.
     */
    fun getList(id: Int?): List = runBlocking {
        return@runBlocking listRepository.get(id)
    }

    /**
     * Gets all lists from listRepository.
     */
    fun getAllLists(): LiveData<kotlin.collections.List<List>> {
        return listRepository.getAll()
    }
}