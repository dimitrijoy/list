package com.joy.list.utils

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * This class defines a note.
 *
 * Notes are always given text by the user but can optionally be provided a title, assigned a due
 * date or reminder as well.
 */
@Entity(tableName = Note.TABLE_NAME)
class Note {
    @PrimaryKey(autoGenerate = true)
    private var id: Int? = null

    @ColumnInfo(name = "title")
    private var title: String? = null

    @ColumnInfo(name = "text")
    private var text: String? = null

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

    /**
     * Stores the constant, static variables of the class, so to speak.
     */
    companion object {
        const val TABLE_NAME = "notes"

        const val EMPTY_NOTE = "Empty note"
    }

    /**
     * Gets the ID of a note.
     */
    fun getId(): Int? {
        return id
    }

    /**
     * Sets the ID of a note.
     */
    fun setId(id: Int?) {
        this.id = id
    }

    /**
     * Gets the title of a note.
     */
    fun getTitle(): String? {
        return title
    }

    /**
     * Sets the title of a note.
     */
    fun setTitle(title: String?) {
        this.title = title
    }

    /**
     * Gets the text of a note.
     */
    fun getText(): String? {
        return text
    }

    /**
     * Sets the text of a note.
     */
    fun setText(text: String?) {
        this.text = text
    }

    /**
     * Clears the due date of a note.
     */
    fun clearDueDate() {
        dueDate = null
        dueDateInMillis = 12345678987654321L
    }

    /**
     * Gets the due date of a note.
     */
    fun getDueDate(): String? {
        return dueDate
    }

    /**
     * Sets the due date of a note.
     */
    fun setDueDate(dueDate: String?) {
        this.dueDate = dueDate
    }

    /**
     * Gets the due date in milliseconds of a note.
     */
    fun getDueDateInMillis(): Long? {
        return dueDateInMillis
    }

    /**
     * Sets the due date in milliseconds of a note.
     */
    fun setDueDateInMillis(dueDateInMillis: Long?) {
        this.dueDateInMillis = dueDateInMillis
    }

    /**
     * Clears the reminder of a note.
     */
    fun clearReminder() {
        reminder = null
        reminderHour = null
        reminderMinute = null
        reminderId = null
    }

    /**
     * Gets the reminder of a note.
     */
    fun getReminder(): String? {
        return reminder
    }

    /**
     * Sets the reminder of a note.
     */
    fun setReminder(reminder: String?) {
        this.reminder = reminder
    }

    /**
     * Gets the reminder hour of a note.
     */
    fun getReminderHour(): Int? {
        return reminderHour
    }

    /**
     * Sets the reminder hour of a note.
     */
    fun setReminderHour(reminderHour: Int?) {
        this.reminderHour = reminderHour
    }

    /**
     * Gets the reminder minute of a note.
     */
    fun getReminderMinute(): Int? {
        return reminderMinute
    }

    /**
     * Sets the reminder minute of a note.
     */
    fun setReminderMinute(reminderMinute: Int?) {
        this.reminderMinute = reminderMinute
    }

    /**
     * Gets the reminder ID of a note.
     */
    fun getReminderId(): Int? {
        return reminderId
    }

    /**
     * Sets the reminder ID of a note.
     */
    fun setReminderId(reminderId: Int?) {
        this.reminderId = reminderId
    }
}