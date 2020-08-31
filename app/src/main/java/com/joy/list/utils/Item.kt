package com.joy.list.utils

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * This class defines an item.
 *
 * Items are always given a title by the user but can optionally be assigned a due date or reminder
 * as well. Users can mark items as complete or incomplete and, in addition, can add and remove
 * items from Favorites. The list ID acts as a foreign key to relate a particular item to the list
 * that it is in.
 */
@Entity(tableName = Item.TABLE_NAME)
class Item(title: String?, listId: Int?) {
    @PrimaryKey(autoGenerate = true)
    private var id: Int? = null

    @ColumnInfo(name = "title")
    private var title: String? = title

    @ColumnInfo(name = "due_date")
    private var dueDate: String? = null

    @ColumnInfo(name = "due_date_in_millis")
    private var dueDateInMillis: Long? = 12345678987654321L

    @ColumnInfo(name = "reminder")
    private var reminder: String? = null

    @ColumnInfo(name = "reminder_hour")
    private var reminderHour: Int? = null

    @ColumnInfo(name = "reminder_minute")
    private var reminderMinute: Int? = null

    @ColumnInfo(name = "reminder_id")
    private var reminderId: Int? = null

    @ColumnInfo(name = "complete")
    private var complete: Boolean? = INCOMPLETE

    @ColumnInfo(name = "favorite")
    private var favorite: Boolean? = UNFAVORITE

    @ColumnInfo(name = "list_id")
    private var listId: Int? = listId

    /**
     * Stores the constant, static variables of the class, so to speak.
     */
    companion object {
        const val TABLE_NAME = "items"

        const val COMPLETE = true
        const val INCOMPLETE = false
        const val FAVORITE = true
        const val UNFAVORITE = false
    }

    /**
     * Alternative constructor for an empty item.
     */
    constructor() : this(null, null)

    /**
     * Gets the ID of an item.
     */
    fun getId(): Int? {
        return id
    }

    /**
     * Sets the ID of an item.
     */
    fun setId(id: Int?) {
        this.id = id
    }

    /**
     * Gets the title of an item.
     */
    fun getTitle(): String? {
        return title
    }

    /**
     * Sets the title of an item.
     */
    fun setTitle(title: String?) {
        this.title = title
    }

    /**
     * Clears the due date of an item.
     */
    fun clearDueDate() {
        dueDate = null
        dueDateInMillis = 12345678987654321L
    }

    /**
     * Gets the due date of an item.
     */
    fun getDueDate(): String? {
        return dueDate
    }

    /**
     * Sets the due date of an item.
     */
    fun setDueDate(dueDate: String?) {
        this.dueDate = dueDate
    }

    /**
     * Gets the due date in milliseconds of an item.
     */
    fun getDueDateInMillis(): Long? {
        return dueDateInMillis
    }

    /**
     * Sets the due date in milliseconds of an item.
     */
    fun setDueDateInMillis(dueDateInMillis: Long?) {
        this.dueDateInMillis = dueDateInMillis
    }

    /**
     * Clears the reminder of an item.
     */
    fun clearReminder() {
        reminder = null
        reminderHour = null
        reminderMinute = null
        reminderId = null
    }

    /**
     * Gets the reminder of an item.
     */
    fun getReminder(): String? {
        return reminder
    }

    /**
     * Sets the reminder of an item.
     */
    fun setReminder(reminder: String?) {
        this.reminder = reminder
    }

    /**
     * Gets the reminder hour of an item.
     */
    fun getReminderHour(): Int? {
        return reminderHour
    }

    /**
     * Sets the reminder hour of an item.
     */
    fun setReminderHour(reminderHour: Int?) {
        this.reminderHour = reminderHour
    }

    /**
     * Gets the reminder minute of an item.
     */
    fun getReminderMinute(): Int? {
        return reminderMinute
    }

    /**
     * Sets the reminder minute of an item.
     */
    fun setReminderMinute(reminderMinute: Int?) {
        this.reminderMinute = reminderMinute
    }

    /**
     * Gets the reminder ID of an item.
     */
    fun getReminderId(): Int? {
        return reminderId
    }

    /**
     * Sets the reminder ID of an item.
     */
    fun setReminderId(reminderId: Int?) {
        this.reminderId = reminderId
    }

    /**
     * Gets the completed status of an item.
     */
    fun isComplete(): Boolean? {
        return complete
    }

    /**
     * Marks an item as complete.
     */
    fun markComplete() { complete = COMPLETE }

    /**
     * Marks an item as incomplete.
     */
    fun markIncomplete() { complete = INCOMPLETE }

    /**
     * Marks an item as either complete or incomplete, depending on the given boolean.
     */
    fun mark(complete: Boolean?) { this.complete = complete }

    /**
     * For Room.
     */
    fun setComplete(complete: Boolean?) { this.complete = complete }

    /**
     * Gets the favorited status of an item.
     */
    fun isFavorite(): Boolean? {
        return favorite
    }

    /**
     * Adds an item to Favorites.
     */
    fun favorite() { favorite = FAVORITE }

    /**
     * Removes an item from Favorites.
     */
    fun unfavorite() { favorite = UNFAVORITE }

    /**
     * Adds or removes an item from Favorites, depending on the given boolean. Naming convention
     * to satisfy Room.
     */
    fun setFavorite(favorite: Boolean?) { this.favorite = favorite }

    /**
     * Gets the ID of the list that an item is in.
     */
    fun getListId(): Int? {
        return listId
    }

    /**
     * Sets the ID of the list that an item is in.
     */
    fun setListId(listId: Int?) {
        this.listId = listId
    }
}