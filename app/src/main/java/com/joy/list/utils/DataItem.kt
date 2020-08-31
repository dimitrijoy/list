package com.joy.list.utils

/**
 * Abstracts items, complete items, and headers to data items for ItemAdapter.
 */
sealed class DataItem {
    abstract val id: Int?

    /**
     * Abstracts items and complete items.
     */
    data class ItemItem(val item: Item): DataItem()      {
        override val id = item.getId()
    }

    /**
     * Abstracts the list header.
     */
    object ListHeader: DataItem() {
        override val id = Int.MIN_VALUE
    }

    /**
     * Abstracts the completed header.
     */
    object CompletedHeader: DataItem() {
        override val id = Int.MAX_VALUE
    }
}