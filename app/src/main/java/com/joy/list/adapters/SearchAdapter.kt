package com.joy.list.adapters

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import com.joy.list.R
import com.joy.list.utils.Item

import java.text.SimpleDateFormat
import java.util.*

/**
 * Handles search results within a RecyclerView.
 */
class SearchAdapter(private val onSearchListener: OnSearchListener) : RecyclerView.Adapter<SearchAdapter.SearchHolder>() {
    private var lists: List<com.joy.list.utils.List> = emptyList()
    private var searchResults: List<Item> = emptyList()

    /**
     * Initializes the adapter's view holder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchAdapter.SearchHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return SearchHolder(itemView, onSearchListener)
    }

    /**
     * Displays the title, list, due date, and reminder of a search result.
     */
    override fun onBindViewHolder(holder: SearchAdapter.SearchHolder, position: Int) {
        val searchResult: Item = searchResults[position]

        holder.textSearchResultTitle.text = searchResult.getTitle()

        showCompletedStatus(searchResult, holder)
        showDueDate(searchResult, holder)
        showList(position, holder)
        showReminder(searchResult, holder)
    }

    /**
     * Gets the number of search results.
     */
    override fun getItemCount() = searchResults.size

    /**
     * Sets the lists in ItemAdapter.
     */
    fun setLists(lists: List<com.joy.list.utils.List>) { this.lists = lists }

    /**
     * Sets the search results in SearchAdapter.
     */
    fun setSearchResults(searchResults: List<Item>) {
        this.searchResults = searchResults
        notifyDataSetChanged()
    }

    /**
     * Shows the completed status of a search result.
     */
    private fun showCompletedStatus(searchResult: Item, holder: SearchHolder) {
        if (searchResult.isComplete()!!) {
            holder.textSearchResultTitle.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        } else {
            holder.textSearchResultTitle.apply {
                paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    /**
     * Shows the due date of a search result if not null.
     */
    private fun showDueDate(item: Item, holder: SearchHolder) {
        if (item.getDueDate() == null) {
            holder.linearDueDate.visibility = View.GONE
        } else {
            holder.linearDueDate.visibility = View.VISIBLE

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = item.getDueDateInMillis()!!

            val daysSinceEpoch = (calendar.get(Calendar.YEAR) - 1970) * 365
            val daysSinceEpochCurrent = (Calendar.getInstance().get(Calendar.YEAR) - 1970) * 365

            val dayOfYear: Int = SimpleDateFormat("D", Locale.US).format(Calendar.getInstance().time).toInt() + daysSinceEpochCurrent
            val dueDate: String = when (calendar.get(Calendar.DAY_OF_YEAR) + daysSinceEpoch) {
                dayOfYear - 1 -> "Yesterday"
                dayOfYear -> "Today"
                dayOfYear + 1 -> "Tomorrow"
                else -> item.getDueDate()!!
            }

            if (calendar.get(Calendar.DAY_OF_YEAR) + daysSinceEpoch < dayOfYear && !item.isComplete()!!) {
                holder.imageCalendar.setColorFilter(
                    ContextCompat.getColor(holder.imageCalendar.context, R.color.holoRedLight),
                    PorterDuff.Mode.SRC_ATOP
                )
                holder.textDueDate.setTextColor(ContextCompat.getColor(holder.imageCalendar.context, R.color.holoRedLight))
            } else {
                holder.imageCalendar.setColorFilter(
                    ContextCompat.getColor(holder.imageCalendar.context, R.color.colorAccent),
                    PorterDuff.Mode.SRC_ATOP
                )
                holder.textDueDate.setTextColor(ContextCompat.getColor(holder.imageCalendar.context, R.color.colorAccent))
            }

            holder.textDueDate.text = dueDate
        }
    }

    /**
     * Shows the list of a search result.
     */
    private fun showList(position: Int, holder: SearchAdapter.SearchHolder) {
        val list = lists[position]
        holder.linearListTitle.visibility = View.VISIBLE
        holder.textListTitle.text = list.getTitle()
    }

    /**
     * Shows the reminder of a search result if not null.
     */
    private fun showReminder(searchResult: Item, holder: SearchHolder) {
        if (searchResult.getReminder() == null) {
            holder.linearReminder.visibility = View.GONE
        } else {
            holder.linearReminder.visibility = View.VISIBLE
            holder.textReminder.text = searchResult.getReminder()
        }
    }

    /**
     * Implements the listeners for search results.
     */
    interface OnSearchListener {
        /**
         * Calls when a search result is clicked.
         */
        fun onSearchResultClick(searchResult: Item)
    }

    /**
     * Holds the views of itemView for each item. Sets the item listener.
     */
    inner class SearchHolder(itemView: View, listener: OnSearchListener) : RecyclerView.ViewHolder(itemView) {
        val imageCalendar: ImageView = itemView.findViewById(R.id.image_calendar)
        val linearDueDate: LinearLayout = itemView.findViewById(R.id.linear_due_date)
        val linearListTitle: LinearLayout = itemView.findViewById(R.id.linear_list_title)
        val linearReminder: LinearLayout = itemView.findViewById(R.id.linear_reminder)
        val textDueDate : TextView = itemView.findViewById(R.id.text_due_date)
        val textListTitle: TextView = itemView.findViewById(R.id.text_list_title)
        val textSearchResultTitle : TextView = itemView.findViewById(R.id.text_search_result_title)
        val textReminder : TextView = itemView.findViewById(R.id.text_reminder)

        private val onSearchListener: OnSearchListener = listener

        init {
            itemView.setOnClickListener {
                onSearchListener.onSearchResultClick(searchResults[absoluteAdapterPosition])
            }
        }
    }
}