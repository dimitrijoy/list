package com.joy.list.activities

import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.google.gson.Gson

import com.joy.list.R
import com.joy.list.adapters.ItemAdapter
import com.joy.list.models.ItemViewModel
import com.joy.list.utils.ActivityHelper
import com.joy.list.utils.Item
import com.joy.list.utils.List
import java.text.SimpleDateFormat
import java.util.*

class ItemActivity : AppCompatActivity() {
    private lateinit var list: List

    private lateinit var item: Item
    private lateinit var itemViewModel: ItemViewModel

    /**
     * Stores the constant, static components of the activity, so to speak.
     */
    companion object {
        private const val DISABLED: Boolean = false
        private const val ENABLED: Boolean = true
    }

    /**
     * Initializes the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        list = Gson().fromJson<List>(intent.getStringExtra(ListActivity.EXTRA_LIST), List::class.java)
        item = Gson().fromJson<Item>(intent.getStringExtra(ListActivity.EXTRA_ITEM), Item::class.java)

        try {
            val fromReminder: Boolean = Gson().fromJson<Boolean>(intent.getStringExtra(ListActivity.EXTRA_FROM_REMINDER), Boolean::class.java)
            if (fromReminder)
                ActivityHelper.dismissReminder(this, item)
        } catch (e: IllegalStateException) {
            Log.e("error!", "IllegalStateException: Boolean extra 'fromReminder' cannot be null (it can).")
        }

        ActivityHelper.setStatusBarColor(this, R.color.colorSecondary)
        ActivityHelper.setSupportActionBar(this)
        ActivityHelper.setToolbarColor(this, R.color.colorSecondary)

        initializeViewModel()
        updateActivity()
    }

    /**
     * Finishes the activity. Saves the changes made to an item.
     */
    override fun finish() {
        super.finish()

        val editItemTitle: EditText = findViewById(R.id.edit_item_title)
        if (editItemTitle.text.toString().trim().isNotEmpty())
            item.setTitle(editItemTitle.text.toString().trim())

        if (item.getReminderId() != null)
            ActivityHelper.setReminder(this, list, item)

        itemViewModel.updateItem(item)
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
     * Deletes an item with a confirmation dialog.
     */
    fun deleteItem(view: View?) {
        val dialogDeleteConfirmation: View = layoutInflater.inflate(R.layout.dialog_delete_confirmation, null)
        val textWarning: TextView = dialogDeleteConfirmation.findViewById(R.id.text_warning)
        textWarning.setText(R.string.msg_delete_item)

        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.DeleteConfirmationDialogTheme)
            .setTitle(R.string.title_delete_item)
            .setView(dialogDeleteConfirmation)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                finish()
                itemViewModel.deleteItem(item)

                if (item.getReminderId() != null)
                    ActivityHelper.dismissReminder(this, item)
            }
            .setNegativeButton(R.string.action_cancel) { dialogInterface, _ ->
                dialogInterface.cancel()
            }

        builder.show()
    }

    /**
     * Favorites/Unfavorites an item.
     */
    fun favoriteItem(view: View?) {
        val editItemTitle: EditText = findViewById(R.id.edit_item_title)
        editItemTitle.clearFocus()

        ActivityHelper.hideKeyboard(this, findViewById(android.R.id.content))

        item.setFavorite(!item.isFavorite()!!)
        itemViewModel.updateItem(item)
        setItemFavoriteStatus()
    }

    /**
     * Marks an item either completed or uncompleted.
     */
    fun markItem(view: View?) {
        val editItemTitle: EditText = findViewById(R.id.edit_item_title)
        editItemTitle.clearFocus()

        item.mark(!item.isComplete()!!)
        itemViewModel.updateItem(item)
        setItemCompleteStatus()
    }

    /**
     * Shows a DatePicker dialog for assigning a due date.
     */
    fun showDatePickerDialog(view: View?) {
        val editItemTitle: EditText = findViewById(R.id.edit_item_title)
        editItemTitle.clearFocus()

        val dialogDatePicker: View = View.inflate(this, R.layout.dialog_date_picker, null)

        ActivityHelper.hideKeyboard(this, view)

        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.DialogTheme)
            .setView(dialogDatePicker)
            .setPositiveButton(R.string.action_set) {_, _ ->
                setDueDate(dialogDatePicker)
                setItemDueDate()
            }
            .setNegativeButton(R.string.action_cancel) {dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .setNeutralButton(R.string.action_clear) {_, _ ->
                clearDueDate()
                setItemDueDate()
            }

        builder.show()
    }

    /**
     * Shows a TimePicker dialog for setting a reminder.
     */
    fun showTimePickerDialog(view: View?) {
        val editItemTitle: EditText = findViewById(R.id.edit_item_title)
        editItemTitle.clearFocus()

        val dialogTimePicker: View = View.inflate(this, R.layout.dialog_time_picker, null)

        ActivityHelper.hideKeyboard(this, view)

        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.DialogTheme)
            .setView(dialogTimePicker)
            .setPositiveButton(R.string.action_set) {_, _ ->
                setReminder(dialogTimePicker)
                setItemReminder()
            }
            .setNegativeButton(R.string.action_cancel) {dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .setNeutralButton(R.string.action_clear) {_, _ ->
                clearReminder()
                setItemReminder()
            }

        builder.show()
    }

    /**
     * Clears the due date of an item.
     */
    private fun clearDueDate() {
        item.clearDueDate()
        itemViewModel.updateItem(item)
    }

    /**
     * Clears the reminder of an item.
     */
    private fun clearReminder() {
        if (item.getReminderId() != null)
            ActivityHelper.dismissReminder(this, item)

        item.clearReminder()
        itemViewModel.updateItem(item)
    }

    /**
     * Initializes the activity's view model.
     */
    private fun initializeViewModel() {
        itemViewModel = ViewModelProvider(this, ViewModelProvider
            .AndroidViewModelFactory.getInstance(application))
            .get(ItemViewModel::class.java)
    }

    /**
     * Assigns an item a due date.
     */
    private fun setDueDate(view: View) {
        val datePicker: DatePicker = view.findViewById(R.id.date_picker)

        val calendar: Calendar = GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth)

        item.setDueDate(SimpleDateFormat("EE, MMM d", Locale.US).format(calendar.time))
        item.setDueDateInMillis(calendar.timeInMillis)

        itemViewModel.updateItem(item)
    }

    /**
     * Sets the complete status of the item within the layout of the activity. If an item
     * is complete, all fields are disabled.
     */
    private fun setItemCompleteStatus() {
        val buttonItemDueDate: Button = findViewById(R.id.button_item_due_date)
        val buttonItemReminder: Button = findViewById(R.id.button_item_reminder)

        val editItemTitle: EditText = findViewById(R.id.edit_item_title)

        val imageMark: ImageButton = findViewById(R.id.image_mark)

        if (item.isComplete()!!) {
            editItemTitle.isEnabled = DISABLED
            editItemTitle.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }

            buttonItemDueDate.isEnabled = DISABLED
            buttonItemReminder.isEnabled = DISABLED

            imageMark.setImageResource(R.drawable.ic_undo)
        } else {
            editItemTitle.isEnabled = ENABLED
            editItemTitle.apply {
                paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            buttonItemDueDate.isEnabled = ENABLED
            buttonItemReminder.isEnabled = ENABLED

            imageMark.setImageResource(R.drawable.ic_check)
        }
    }

    /**
     * Sets the item due date within the layout of the activity.
     */
    private fun setItemDueDate() {
        val buttonItemDueDate: Button = findViewById(R.id.button_item_due_date)
        buttonItemDueDate.text = item.getDueDate() ?: resources.getString(R.string.title_due_date)
    }

    /**
     * Sets the favorite status of the item within the layout of the activity.
     */
    private fun setItemFavoriteStatus() {
        val imageFavorite: ImageButton = findViewById(R.id.image_favorite)
        if (item.isFavorite()!!)
            imageFavorite.setImageResource(R.drawable.ic_star)
        else
            imageFavorite.setImageResource(R.drawable.ic_star_border)
    }

    /**
     * Sets the item reminder within the layout of the activity.
     */
    private fun setItemReminder() {
        val buttonItemReminder: Button = findViewById(R.id.button_item_reminder)
        buttonItemReminder.text = item.getReminder() ?: resources.getString(R.string.title_reminder)
    }

    /**
     * Sets the item title within the layout of the activity.
     */
    private fun setItemTitle() {
        val editItemTitle: EditText = findViewById(R.id.edit_item_title)
        editItemTitle.setText(item.getTitle())
        editItemTitle.imeOptions = EditorInfo.IME_ACTION_DONE
        editItemTitle.setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        editItemTitle.clearFocus()
    }

    /**
     * Sets an item a reminder.
     */
    private fun setReminder(view: View) {
        val timePicker: TimePicker = view.findViewById(R.id.time_picker)

        val hour: Int = if (Build.VERSION.SDK_INT >= 23) timePicker.hour else timePicker.currentHour
        val minute: Int = if (Build.VERSION.SDK_INT >= 23) timePicker.minute else timePicker.currentMinute
        val hourString: String = when {
            hour > 12 -> (hour % 12).toString()
            hour == 0 -> "12"
            else -> hour.toString()
        }
        val minuteString: String = if (minute < 10) "0$minute" else minute.toString()
        val period: String = if (hour < 12) "AM" else "PM"

        item.setReminder("$hourString:$minuteString $period")
        item.setReminderHour(hour)
        item.setReminderMinute(minute)
        item.setReminderId(((Date().time / 1000L) % Int.MAX_VALUE).toInt())

        itemViewModel.updateItem(item)
    }

    /**
     * Updates the activity with the most recent details of a particular item.
     */
    private fun updateActivity() {
        setItemTitle()
        setItemDueDate()
        setItemReminder()
        setItemCompleteStatus()
        setItemFavoriteStatus()
    }
}