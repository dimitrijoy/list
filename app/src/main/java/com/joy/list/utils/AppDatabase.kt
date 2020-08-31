package com.joy.list.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.joy.list.interfaces.ItemDao
import com.joy.list.interfaces.ListDao
import com.joy.list.interfaces.NoteDao

/**
 * Defines the database for the app.
 */
@Database(entities = [List::class, Item::class, Note::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun listDao(): ListDao
    abstract fun itemDao(): ItemDao
    abstract fun noteDao(): NoteDao

    /**
     * Stores the constant, static members of the class, so to speak.
     */
    companion object {
        private const val DATABASE_NAME = "app_db"

        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Builds the database for the app if it does not already exist. Only one instance of the
         * database can be active at any moment.
         */
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance
                    ?: buildDatabase(context).also { instance = it }
            }
        }

        /**
         * Builds the database for the app.
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .build()
        }
    }
}