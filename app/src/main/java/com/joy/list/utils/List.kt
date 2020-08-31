package com.joy.list.utils


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import java.io.Serializable

/**
 * This class defines a list.
 *
 * Lists are given a title and color by the user and are used to store items.
 */
@Entity(tableName = List.TABLE_NAME)
class List(title: String, color: String) {
    @PrimaryKey(autoGenerate = true)
    private var id: Int? = null

    @ColumnInfo(name = "title")
    private var title: String? = title

    @ColumnInfo(name = "color")
    private var color: String? = color

    @ColumnInfo(name = "sort_by")
    private var sortBy: Int? = ORDER_CREATED

    /**
     * Stores the constant, static variables of the class, so to speak.
     */
    companion object {
        const val TABLE_NAME = "lists"

        const val UNTITLED = "Untitled list"
        const val ACCENT_COLOR = "#FFFFFF"
        const val ALPHABETICAL_ORDER = 0
        const val DUE_DATE = 1
        const val ORDER_CREATED = 2
    }

    /**
     * Alternative constructor for a default list.
     */
    constructor() : this(UNTITLED, ACCENT_COLOR)

    /**
     * Gets the ID of a list.
     */
    fun getId(): Int? {
        return id
    }

    /**
     * Sets the ID of a list.
     */
    fun setId(id: Int?) {
        this.id = id
    }

    /**
     * Gets the title of a list.
     */
    fun getTitle(): String? {
        return title
    }

    /**
     * Sets the title of a list.
     */
    fun setTitle(title: String?) {
        this.title = title
    }

    /**
     * Gets the color of a list.
     */
    fun getColor(): String? {
        return color
    }

    /**
     * Sets the color of a list.
     */
    fun setColor(color: String?) {
        this.color = color
    }

    /**
     * Gets the order that a list is sorted by.
     */
    fun sortedBy(): Int? {
        return sortBy
    }

    /**
     * For Room.
     */
    fun getSortBy(): Int? {
        return sortBy
    }

    /**
     * Sorts a list in a given order.
     */
    fun sortBy(sortBy: Int?) {
        this.sortBy = sortBy
    }
}