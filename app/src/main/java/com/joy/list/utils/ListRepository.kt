package com.joy.list.utils

import android.app.Application
import androidx.lifecycle.LiveData

import com.joy.list.interfaces.ListDao

/**
 * Provides a layer of abstraction for ListDao.
 */
class ListRepository(application: Application) {
    private val listDao: ListDao

    /**
     * Initializes an instance of ListDao.
     */
    init {
        val appDatabase: AppDatabase = AppDatabase.getInstance(application)
        listDao = appDatabase.listDao()
    }

    /**
     * Gets all lists from listDao.
     */
    fun getAll(): LiveData<kotlin.collections.List<List>> {
        return listDao.getAll()
    }

    /**
     * Gets a list with a particular id.
     */
    suspend fun get(id: Int?): List {
        return listDao.get(id)
    }

    /**
     * Passes a list to be inserted by listDao.
     */
    suspend fun insert(list: List) {
        listDao.insert(list)
    }

    /**
     * Passes a list to be updated by listDao.
     */
    suspend fun update(list: List) {
        listDao.update(list)
    }

    /**
     * Passes a list to be deleted by listDao.
     */
    suspend fun delete(list: List) {
        listDao.delete(list)
    }
}