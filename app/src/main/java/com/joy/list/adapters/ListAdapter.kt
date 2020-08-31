package com.joy.list.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.joy.list.R
import com.joy.list.utils.List

/**
 * Handles lists within a RecyclerView.
 */
class ListAdapter(listener: OnListListener) : RecyclerView.Adapter<ListAdapter.ListHolder>() {
    private var lists: kotlin.collections.List<List> = emptyList()
    private val onListListener: OnListListener = listener

    /**
     * Initializes the adapter's view holder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ListHolder(itemView, onListListener)
    }

    /**
     * Displays the title and color of a list in an itemView.
     */
    override fun onBindViewHolder(holder: ListHolder, position: Int) {
        val currentList = lists[position]
        holder.textListColor.setTextColor(Color.parseColor(currentList.getColor()))
        holder.textListTitle.text = currentList.getTitle()
    }

    /**
     * Gets the number of lists.
     */
    override fun getItemCount() = lists.size

    /**
     * Sets the lists in ListAdapter.
     */
    fun setLists(lists: kotlin.collections.List<List>) {
        this.lists = lists
        notifyDataSetChanged()
    }

    /**
     * Implements the listeners for lists. The listener currently only calls when list is clicked.
     */
    interface OnListListener {
        /**
         * Calls when a list is clicked.
         */
        fun onListClick(list: List)
    }

    /**
     * Holds the views of itemView for each list. Sets the list listener.
     */
    inner class ListHolder(itemView: View, listener: OnListListener) : RecyclerView.ViewHolder(itemView) {
        val textListColor: TextView = itemView.findViewById(R.id.text_list_color)
        val textListTitle: TextView = itemView.findViewById(R.id.text_list_title)

        private val onListListener: OnListListener = listener

        init {
            itemView.setOnClickListener { _ ->
                onListListener.onListClick(lists[adapterPosition])
            }
        }
    }
}