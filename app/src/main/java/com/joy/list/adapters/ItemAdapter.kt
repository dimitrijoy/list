package com.joy.list.adapters

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.joy.list.R
import com.joy.list.utils.DataItem
import com.joy.list.utils.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

/**
 * Handles items within a RecyclerView.
 */
class ItemAdapter(private val onItemListener: OnItemListener,
                  private val list: com.joy.list.utils.List,
                  private val quickAccess: Boolean) : ListAdapter<DataItem, RecyclerView.ViewHolder>(ItemDiffCallback()) {
    private var lists: List<com.joy.list.utils.List> = emptyList()

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    /**
     * Stores the constant, static components of the adapter, so to speak.
     */
    companion object {
        private const val ITEM_VIEW_TYPE_LIST_HEADER = 0
        private const val ITEM_VIEW_TYPE_COMPLETED_HEADER = 1
        private const val ITEM_VIEW_TYPE_ITEM = 2
    }

    /**
     * An alternative constructor.
     */
    constructor(onItemListener: OnItemListener, list: com.joy.list.utils.List) : this(onItemListener, list, false)

    /**
     * Initializes the adapter's view holder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_LIST_HEADER -> ListHeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_COMPLETED_HEADER -> CompletedHeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    /**
     * Displays the title, due date, and reminder of an item in an itemView.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val item = getItem(position) as DataItem.ItemItem
            holder.setOnItemListener(item.item, onItemListener)

            holder.textItemTitle.text = item.item.getTitle()

            setCheckbox(item.item, holder)
            setFavoriteButton(item.item, holder)
            showDueDate(item.item, holder)
            showList(position, holder)
            showReminder(item.item, holder)
        } else if (holder is ListHeaderViewHolder) {
            holder.textHeader.text = list.getTitle()
        }
    }

    /**
     * Gets the type of a data item.
     */
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.ListHeader -> ITEM_VIEW_TYPE_LIST_HEADER
            is DataItem.CompletedHeader -> ITEM_VIEW_TYPE_COMPLETED_HEADER
            is DataItem.ItemItem -> ITEM_VIEW_TYPE_ITEM
            else -> throw IllegalArgumentException("Unknown viewType $position")
        }
    }

    /**
     * Updates items in adapter.
     */
    fun addHeaderAndSubmitList(items: List<Item>?) {
        adapterScope.launch {
            val dataItems = items?.map { DataItem.ItemItem(it) }

            withContext(Dispatchers.Main) {
                submitList(dataItems)
            }
        }
    }

    /**
     * Updates items in adapter.
     */
    fun addHeaderAndSubmitList(items: List<Item>?, completeItems: List<Item>?) {
        adapterScope.launch {
            val dataItems: MutableList<DataItem> = mutableListOf(DataItem.ListHeader)

            if (items!!.isNotEmpty())
                dataItems += items.map { DataItem.ItemItem(it) }

            if (completeItems!!.isNotEmpty()) {
                dataItems += listOf(DataItem.CompletedHeader)
                dataItems += completeItems.map { DataItem.ItemItem(it) }
            }

            withContext(Dispatchers.Main) {
                submitList(dataItems)
            }
        }
    }

    /**
     * Gets the item at a specified index.
     */
    fun getItemAt(position: Int): DataItem = getItem(position)

    /**
     * Sets the lists in ItemAdapter.
     */
    fun setLists(lists: List<com.joy.list.utils.List>) { this.lists = lists }
    
    /**
     * Shows the due date of an item if not null.
     */
    private fun showDueDate(item: Item, holder: ViewHolder) {
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
     * Shows the list that an item is within if in quick access.
     */
    private fun showList(position: Int, holder: ViewHolder) {
        if (quickAccess) {
            val list = lists[position]
            holder.linearListTitle.visibility = View.VISIBLE
            holder.textListTitle.text = list.getTitle()
        } else {
            holder.linearListTitle.visibility = View.GONE
        }
    }

    /**
     * Shows the reminder of an item if not null.
     */
    private fun showReminder(item: Item, holder: ViewHolder) {
        if (item.getReminder() == null) {
            holder.linearReminder.visibility = View.GONE
        } else {
            holder.linearReminder.visibility = View.VISIBLE
            holder.textReminder.text = item.getReminder()
        }
    }

    /**
     * Checks/unchecks the checkbox depending on an item's complete status.
     */
    private fun setCheckbox(item: Item, holder: ViewHolder) {
        if (item.isComplete()!!) {
            holder.imageCheckbox.setImageResource(R.drawable.ic_checkbox)
            holder.imageCheckbox.setColorFilter(
                Color.parseColor(list.getColor()),
                PorterDuff.Mode.SRC_ATOP
            )
            holder.textItemTitle.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        } else {
            holder.imageCheckbox.setImageResource(R.drawable.ic_checkbox_border)
            holder.imageCheckbox.setColorFilter(
                Color.parseColor(list.getColor()),
                PorterDuff.Mode.SRC_ATOP
            )
            holder.textItemTitle.apply {
                paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    /**
     * Enables/disables the favorite button depending on an item's favorite status.
     */
    private fun setFavoriteButton(item: Item, holder: ViewHolder) {
        if (item.isFavorite()!!)
            holder.imageFavorite.setImageResource(R.drawable.ic_star)
        else {
            holder.imageFavorite.setImageResource(R.drawable.ic_star_border)
        }
    }

    /**
     * Implements the listeners for items.
     */
    interface OnItemListener {
        /**
         * Calls when an item is clicked.
         */
        fun onItemClick(item: Item)

        /**
         * Calls when an item is favorited/unfavorited.
         */
        fun onItemFavorite(item: Item, position: Int)

        /**
         * Calls when an item is marked complete/incomplete.
         */
        fun onItemMark(item: Item)
    }

    /**
     * Holds the views for an item.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageCalendar: ImageView = itemView.findViewById(R.id.image_calendar)
        val imageCheckbox: ImageButton = itemView.findViewById(R.id.image_checkbox)
        val imageFavorite: ImageButton = itemView.findViewById(R.id.image_favorite)
        val linearDueDate: LinearLayout = itemView.findViewById(R.id.linear_due_date)
        val linearListTitle: LinearLayout = itemView.findViewById(R.id.linear_list_title)
        val linearReminder: LinearLayout = itemView.findViewById(R.id.linear_reminder)
        val textDueDate : TextView = itemView.findViewById(R.id.text_due_date)
        val textListTitle: TextView = itemView.findViewById(R.id.text_list_title)
        val textItemTitle : TextView = itemView.findViewById(R.id.text_item_title)
        val textReminder : TextView = itemView.findViewById(R.id.text_reminder)

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.item_item, parent, false)

                return ViewHolder(view)
            }
        }

        /**
         * Sets a listener for an item.
         */
        fun setOnItemListener(item: Item, onItemListener: OnItemListener) {
            imageCheckbox.setOnClickListener {
                onItemListener.onItemMark(item)
            }

            itemView.setOnClickListener {
                onItemListener.onItemClick(item)
            }

            imageFavorite.setOnClickListener {
                onItemListener.onItemFavorite(item, absoluteAdapterPosition)
            }
        }
    }

    /**
     * Holds the views of a list header.
     */
    class ListHeaderViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val textHeader: TextView = view.findViewById(R.id.text_header)

        companion object {
            fun from(parent: ViewGroup): ListHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header_items, parent, false)
                return ListHeaderViewHolder(view)
            }
        }
    }


    /**
     * Holds the views of a completed header.
     */
    class CompletedHeaderViewHolder(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): CompletedHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header_complete_items, parent, false)
                return CompletedHeaderViewHolder(view)
            }
        }
    }
}

/**
 * Handles changes in an item.
 */
class ItemDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    /**
     * Indicates if two items are the same with a boolean value.
     */
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return if (oldItem is DataItem.ItemItem && newItem is DataItem.ItemItem) {
            oldItem.item.getId() == newItem.item.getId()
        } else {
            oldItem == newItem
        }
    }

    /**
     * Indicates if two items have the same contents with a boolean value.
     */
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return if (oldItem is DataItem.ItemItem && newItem is DataItem.ItemItem) {
            oldItem.item.getTitle() == newItem.item.getTitle()
                    && oldItem.item.getDueDateInMillis() == newItem.item.getDueDateInMillis()
                    && oldItem.item.getReminderId() == newItem.item.getReminderId()
                    && oldItem.item.isComplete() == newItem.item.isComplete()
                    && oldItem.item.isFavorite() == newItem.item.isFavorite()
        } else {
            oldItem == newItem
        }
    }
}