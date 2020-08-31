package com.joy.list.activities

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson

import com.joy.list.R
import com.joy.list.adapters.ItemAdapter
import com.joy.list.fragments.NewItemFragment
import com.joy.list.models.ItemViewModel
import com.joy.list.models.ListViewModel
import com.joy.list.utils.ActivityHelper
import com.joy.list.utils.DataItem
import com.joy.list.utils.Item
import com.joy.list.utils.List

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

/**
 * Displays the items of a particular list. Provides additional functionality, such as completing,
 * deleting, editing, and favoriting items.
 */
class ListActivity : AppCompatActivity(), ItemAdapter.OnItemListener, NewItemFragment.NewItemFragmentListener {
    private lateinit var list: List
    private lateinit var listViewModel: ListViewModel

    private lateinit var itemAdapter: ItemAdapter
    private lateinit var itemViewModel: ItemViewModel

    private lateinit var listColors: Array<String?>
    private var listColorIndex: Int = MainActivity.INIT_LIST_COLOR_INDEX

    private lateinit var fab: FloatingActionButton

    /**
     * Stores the constant, static components of the activity, so to speak.
     */
    companion object {
        private val TAG: String = ListActivity::class.java.simpleName

        const val EXTRA_LIST: String = "com.joy.activities.ListActivity.EXTRA_LIST"
        const val EXTRA_ITEM: String = "com.joy.activities.ListActivity.EXTRA_ITEM"
        const val EXTRA_FROM_REMINDER: String = "com.joy.activities.ListActivity.EXTRA_FROM_REMINDER"

        /**
         * Gets the intent for starting ListActivity.
         */
        fun getStartIntent(context: Context, destination: Class<*>, list: List, item: Item): Intent {
            val intent = Intent(context, destination)
            intent.putExtra(EXTRA_LIST, Gson().toJson(list))
            intent.putExtra(EXTRA_ITEM, Gson().toJson(item))
            return intent
        }

        /**
         * Gets the intent for starting ListActivity.
         */
        fun getStartIntent(context: Context, destination: Class<*>, list: List, item: Item, fromReminder: Boolean): Intent {
            val intent = Intent(context, destination)
            intent.putExtra(EXTRA_LIST, Gson().toJson(list))
            intent.putExtra(EXTRA_ITEM, Gson().toJson(item))
            intent.putExtra(EXTRA_FROM_REMINDER, Gson().toJson(fromReminder))
            return intent
        }
    }

    /**
     * Initializes the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        list = Gson().fromJson<List>(intent.getStringExtra(MainActivity.EXTRA_LIST), List::class.java)

        fab = findViewById(R.id.fab)
        fab.backgroundTintList = ColorStateList.valueOf(Color.parseColor((list.getColor())))

        ActivityHelper.setStatusBarColor(this, R.color.colorPrimary)
        ActivityHelper.setSupportActionBar(this)
        ActivityHelper.setToolbarColor(this, R.color.colorPrimary)

        initializeViewModels()
        populateItems()
        setListColors()
        setOnScrollListener()
    }

    /**
     * Finishes the activity.
     */
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    /**
     * Initializes the list options menu.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.list_options, menu)
        return true
    }

    /**
     * Allows a user to finish the activity; delete, edit, or sort a list; or delete all items in a
     * list.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.item_edit_list -> { editList(); false }
            R.id.item_delete_list -> { deleteList(); true }
            R.id.item_delete_all_items -> { clearList(); false }
            R.id.item_sort_by -> { sortList(); false }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Opens a clicked item. Starts ItemActivity.
     */
    override fun onItemClick(item: Item) {
        startActivity(getStartIntent(this, ItemActivity::class.java, list, item))
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
        item.mark(!item.isComplete()!!)
        itemViewModel.updateItem(item)
        itemAdapter.notifyDataSetChanged()
    }

    /**
     * Shows the floating action button.
     */
    override fun onCancel() { fab.show() }

    /**
     * Inserts a new item into the database.
     */
    override fun onSave(item: Item) {
        item.setListId(list.getId())

        if (item.getReminderId() != null)
            ActivityHelper.setReminder(this, list, item)

        itemViewModel.insertItem(item)

        fab.show()
    }

    /**
     * Shows a fragment for creating a new item.
     */
    fun showNewItemFragment(view: View) {
        fab.visibility = View.GONE
        list.getColor()?.let { NewItemFragment(it) }?.show(supportFragmentManager, TAG)
    }

    /**
     * Deletes all items from a list with a confirmation dialog.
     */
    private fun clearList() {
        val dialogDeleteConfirmation: View = View.inflate(this, R.layout.dialog_delete_confirmation, null)
        val textWarning: TextView = dialogDeleteConfirmation.findViewById(R.id.text_warning)
        textWarning.setText(R.string.msg_delete_all_items)

        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.DeleteConfirmationDialogTheme)
            .setTitle(R.string.title_delete_all_items)
            .setView(dialogDeleteConfirmation)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                val items: kotlin.collections.List<Item>? = itemViewModel.getAllItemsFrom(list.getId()).value
                if (items != null)
                    for (item in items)
                        if (item.getReminderId() != null)
                            ActivityHelper.dismissReminder(this, item)

                itemViewModel.deleteAllItemsFrom(list.getId())
            }
            .setNegativeButton(R.string.action_cancel) {dialogInterface, _ ->
                dialogInterface.cancel()
            }

        builder.show()
    }

    /**
     * Deletes a list with a confirmation dialog.
     */
    private fun deleteList() {
        val dialogDeleteConfirmation: View = View.inflate(this, R.layout.dialog_delete_confirmation, null)
        val textWarning: TextView = dialogDeleteConfirmation.findViewById(R.id.text_warning)
        textWarning.setText(R.string.msg_delete_list)

        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.DeleteConfirmationDialogTheme)
            .setTitle(R.string.title_delete_list)
            .setView(dialogDeleteConfirmation)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                finish()

                val items: kotlin.collections.List<Item>? = itemViewModel.getAllItemsFrom(list.getId()).value
                if (items != null)
                    for (item in items)
                        if (item.getReminderId() != null)
                            ActivityHelper.dismissReminder(this, item)

                listViewModel.deleteList(list)
                itemViewModel.deleteAllItemsFrom(list.getId())
            }
            .setNegativeButton(R.string.action_cancel) {dialogInterface, _ ->
                dialogInterface.cancel()
            }

        builder.show()
    }

    /**
     * Edits a list with a dialog for user input.
     */
    private fun editList() {
        val dialogNewList: View = View.inflate(this, R.layout.dialog_new_edit_list, null)
        val editListNewTitle: EditText = dialogNewList.findViewById(R.id.edit_new_list_title)
        editListNewTitle.setText(list.getTitle())
        editListNewTitle.setOnFocusChangeListener { _, _ ->
            editListNewTitle.post {
                ActivityHelper.showKeyboard(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        editListNewTitle.requestFocus()

        listColorIndex = getListColorIndex()
        setOnCheckedListColorListener(dialogNewList, getListColorIndex())

        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle(R.string.title_new_list)
            .setView(dialogNewList)
            .setPositiveButton(R.string.action_save) { _, _ ->
                updateList(editListNewTitle.text.toString().trim(), listColors[listColorIndex])
                itemAdapter.notifyDataSetChanged()
                fab.backgroundTintList = ColorStateList.valueOf(Color.parseColor((list.getColor())))
            }
            .setNegativeButton(R.string.action_cancel) { dialogInterface, _ ->
                dialogInterface.cancel()
            }

        builder.show()
    }

    /**
     * Gets the index of a list's color in listColors.
     */
    private fun getListColorIndex(): Int {
        var index = 0
        for (i in listColors.indices)
            if (listColors[i] == list.getColor()) index = i
        return index
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
        itemAdapter = ItemAdapter(this, list)

        val animator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }

        val recyclerItems: RecyclerView = findViewById(R.id.recycler_items)
        recyclerItems.layoutManager = LinearLayoutManager(this)
        recyclerItems.setHasFixedSize(true)
        recyclerItems.itemAnimator = animator
        recyclerItems.adapter = itemAdapter

        itemViewModel.getAllItemsFrom(list.getId(), list.getSortBy()!!).observe(this, Observer { items ->
            val completeItems =  mutableListOf<Item>()
            val incompleteItems =  mutableListOf<Item>()
            for (item in items)
                if (item.isComplete()!!)
                    completeItems.add(item)
                else
                    incompleteItems.add(item)

            itemAdapter.addHeaderAndSubmitList(incompleteItems, completeItems)
        })

        setItemTouchListener(recyclerItems, itemAdapter)
    }

    /**
     * Deletes an item upon being swiped right.
     */
    private fun setItemTouchListener(recyclerView: RecyclerView, itemAdapter: ItemAdapter) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            /**
             * Disables onSwiped() for headers.
             */
            override fun getSwipeDirs (recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                if (viewHolder is ItemAdapter.ListHeaderViewHolder || viewHolder is ItemAdapter.CompletedHeaderViewHolder) return 0
                return super.getSwipeDirs(recyclerView, viewHolder)
            }

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
                    ActivityHelper.dismissReminder(this@ListActivity, item)

                val snackbar = Snackbar.make(findViewById(R.id.fab), R.string.msg_item_deleted, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(applicationContext, R.color.colorSecondary))
                    .setActionTextColor(Color.parseColor(list.getColor()))
                    .setAction(R.string.action_undo) {
                        itemViewModel.insertItem(item)
                        if (item.getReminderId() != null)
                            ActivityHelper.setReminder(this@ListActivity, list, item)
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

    /**
     * Stores the available list colors from colors.xml in a global array.
     */
    private fun setListColors() {
        val temp: IntArray = resources.getIntArray(R.array.list_colors)
        listColors = arrayOfNulls(temp.size)
        for (i in listColors.indices) listColors[i] = String.format("#%06X", 0xFFFFFF and temp[i])
    }

    /**
     * Sets the index of the currently selected list color.
     */
    private fun setOnCheckedListColorListener(view: View, index: Int) {
        val radioListColor: RadioGroup = view.findViewById(R.id.radio_list_color)

        var previousCheckedId: Int = radioListColor.getChildAt(index).id
        radioListColor.check(previousCheckedId)

        val radioButton: RadioButton = view.findViewById(previousCheckedId)
        radioButton.setText(R.string.ic_large_circle)

        radioListColor.setOnCheckedChangeListener { _, checkedId ->
            val previousListColor: RadioButton = view.findViewById(previousCheckedId)
            previousListColor.text = getText(R.string.ic_small_circle)

            val currentListColor: RadioButton = view.findViewById(checkedId)
            currentListColor.text = getText(R.string.ic_large_circle)

            previousCheckedId = checkedId
            listColorIndex = radioListColor.indexOfChild(currentListColor)
        }
    }

    /**
     * Hides the floating action button on scroll down. Shows the floating action button otherwise.
     */
    private fun setOnScrollListener() {
        val recyclerItems: RecyclerView = findViewById(R.id.recycler_items)
        recyclerItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0)
                    fab.hide()
                else
                    fab.show()
            }
        })
    }

    /**
     * Sorts a list with a dialog for user input.
     */
    private fun sortList() {
        val dialogSortBy: View = View.inflate(this, R.layout.dialog_sort_by, null)

        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle(R.string.title_sort_by)
            .setView(dialogSortBy)
            .setPositiveButton(R.string.action_sort) { _, _ ->
                val radioSortBy: RadioGroup = dialogSortBy.findViewById(R.id.radio_sort_by)

                var nothingSelected = false
                if (dialogSortBy.findViewById<RadioButton>(radioSortBy.checkedRadioButtonId) == null)
                    nothingSelected = true

                if (!nothingSelected) {
                    val currentSortBy: RadioButton = dialogSortBy.findViewById(radioSortBy.checkedRadioButtonId)

                    if (list.sortedBy() != radioSortBy.indexOfChild(currentSortBy)) {
                        list.sortBy(radioSortBy.indexOfChild(currentSortBy))
                        listViewModel.updateList(list)
                        itemViewModel.getAllItemsFrom(list.getId(), list.getSortBy()!!).observe(this, Observer { items ->
                            val completeItems =  mutableListOf<Item>()
                            val incompleteItems =  mutableListOf<Item>()
                            for (item in items)
                                if (item.isComplete()!!)
                                    completeItems.add(item)
                                else
                                    incompleteItems.add(item)

                            itemAdapter.addHeaderAndSubmitList(incompleteItems, completeItems)
                        })
                    }
                }
            }
            .setNegativeButton(R.string.action_cancel) { dialogInterface, _ ->
                dialogInterface.cancel()
            }

        builder.show()
    }

    /**
     * Updates a list in the database.
     */
    private fun updateList(listTitle: String, listColor: String?) {
        if (listTitle.isNotEmpty()) list.setTitle(listTitle)
        list.setColor(listColor)

        listViewModel.updateList(list)
    }
}

