package com.joy.list.activities

import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.snackbar.Snackbar

import com.joy.list.R
import com.joy.list.adapters.ItemAdapter
import com.joy.list.models.ItemViewModel
import com.joy.list.models.ListViewModel
import com.joy.list.utils.ActivityHelper
import com.joy.list.utils.DataItem
import com.joy.list.utils.Item
import com.joy.list.utils.List

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


/**
 * Displays the user's items with due dates, sorted by due date.
 */
class UpcomingActivity : AppCompatActivity(), ItemAdapter.OnItemListener {
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var itemViewModel: ItemViewModel

    private val list = List(UPCOMING_TITLE, UPCOMING_COLOR)
    private lateinit var listViewModel: ListViewModel

    /**
     * Stores the constant, static components of the activity, so to speak.
     */
    companion object {
        private const val UPCOMING_COLOR = "#79349c"
        private const val UPCOMING_TITLE = "Upcoming"
    }

    /**
     * Initializes the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upcoming)

        ActivityHelper.setStatusBarColor(this, R.color.colorPrimary)
        ActivityHelper.setSupportActionBar(this)
        ActivityHelper.setToolbarColor(this, R.color.colorPrimary)

        initializeViewModels()
        populateItems()
    }

    /**
     * Finishes the activity.
     */
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    /**
     * Closes the activity.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * Opens a clicked item. Starts ItemActivity.
     */
    override fun onItemClick(item: Item) {
        startActivity(ListActivity.getStartIntent(this, ItemActivity::class.java, listViewModel.getList(item.getListId()), item))
    }

    /**
     * Favorites/Unfavorites an item.
     */
    override fun onItemFavorite(item: Item, position: Int) {
        item.setFavorite(!item.isFavorite()!!)
        itemViewModel.updateItem(item)
        itemAdapter.notifyItemChanged(position)
    }

    /**
     * Marks an item complete/incomplete.
     */
    override fun onItemMark(item: Item) {
        item.markComplete()
        itemViewModel.updateItem(item)

        val snackbar = Snackbar.make(findViewById(R.id.coordinator), R.string.msg_item_completed, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(applicationContext, R.color.colorSecondary))
            .setActionTextColor(Color.parseColor(list.getColor()))
            .setAction(R.string.action_undo) {
                item.markIncomplete()
                itemViewModel.updateItem(item)
            }

        snackbar.show()
    }

    /**
     * Initializes the activity's view models.
     */
    private fun initializeViewModels() {
        listViewModel = ViewModelProvider(this, ViewModelProvider
            .AndroidViewModelFactory.getInstance(application))
            .get(ListViewModel::class.java)

        itemViewModel = ViewModelProvider(this, ViewModelProvider
            .AndroidViewModelFactory.getInstance(application))
            .get(ItemViewModel::class.java)
    }

    /**
     * Populates the items of a particular list.
     */
    private fun populateItems() {
        itemAdapter = ItemAdapter(this, list, true)

        val animator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }

        val recyclerItems: RecyclerView = findViewById(R.id.recycler_upcoming_items)
        recyclerItems.layoutManager = LinearLayoutManager(this)
        recyclerItems.setHasFixedSize(true)
        recyclerItems.itemAnimator = animator
        recyclerItems.adapter = itemAdapter



        itemViewModel.getAllItemsFrom(Item.INCOMPLETE).observe(this, Observer { items ->
            val lists: MutableList<List> = arrayListOf()
            for (item in items)
                lists.add(listViewModel.getList(item.getListId()))

            itemAdapter.setLists(lists)
            itemAdapter.addHeaderAndSubmitList(items)
        })

        setItemTouchListener(recyclerItems, itemAdapter)
    }

    /**
     * Deletes an item upon being swiped right.
     */
    private fun setItemTouchListener(recyclerView: RecyclerView, itemAdapter: ItemAdapter) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            /**
             * Not necessary for this activity.
             */
            override fun onMove(recyclerView: RecyclerView, dragged: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return true
            }

            /**
             * Deletes an item upon being swiped right.
             */
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val dataItem = itemAdapter.getItemAt(viewHolder.absoluteAdapterPosition) as DataItem.ItemItem
                val item: Item = dataItem.item
                itemViewModel.deleteItem(item)

                if (item.getReminderId() != null)
                    ActivityHelper.dismissReminder(this@UpcomingActivity, item)

                val snackbar = Snackbar.make(findViewById(R.id.coordinator), R.string.msg_item_deleted, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(applicationContext, R.color.colorSecondary))
                    .setActionTextColor(Color.parseColor(list.getColor()))
                    .setAction(R.string.action_undo) {
                        itemViewModel.insertItem(item)

                        if (item.getReminderId() != null)
                            ActivityHelper.setReminder(this@UpcomingActivity, list, item)
                    }

                snackbar.show()
            }

            /**
             * Shows a delete icon behind an item upon being swiped right.
             */
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val decorator = RecyclerViewSwipeDecorator(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                decorator.setBackgroundColor(Color.parseColor(list.getColor()))

                if (list.getColor() == List.ACCENT_COLOR)
                    decorator.setActionIconId(R.drawable.ic_delete_default)
                else
                    decorator.setActionIconId(R.drawable.ic_delete)

                decorator.decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }).attachToRecyclerView(recyclerView)
    }
}