package com.joy.list.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.joy.list.R
import com.joy.list.utils.ActivityHelper
import com.joy.list.utils.Item
import com.joy.list.utils.List
import java.text.SimpleDateFormat
import java.util.*

/**
 * Accepts user input for creating new items.
 */
class NewItemFragment(color: String) : BottomSheetDialogFragment() {
    private lateinit var newItemFragmentListener: NewItemFragmentListener

    private val listColor: String = color

    private val item = Item()

    private lateinit var buttonDueDate: Button
    private lateinit var buttonReminder: Button

    /**
     * Stores the constant, static components of the fragment, so to speak.
     */
    companion object {
        private const val DISABLED: Boolean = false
        private const val ENABLED: Boolean = true
    }

    /**
     * Initializes the activity.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_new_item, container, false)

        val editNewItemTitle: EditText = view.findViewById(R.id.edit_new_item_title)
        editNewItemTitle.requestFocus()

        val buttonSave: Button = view.findViewById(R.id.button_save)
        buttonSave.isEnabled = DISABLED

        buttonDueDate = view.findViewById(R.id.button_due_date)
        buttonReminder = view.findViewById(R.id.button_reminder)

        setListeners(editNewItemTitle, buttonSave, buttonDueDate, buttonReminder)

        return view
    }

    /**
     * Attaches the fragment's listener.
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        newItemFragmentListener = context as NewItemFragmentListener
    }

    /**
     * Calls upon the fragment closing.
     */
    override fun onCancel(dialogInterface: DialogInterface) {
        super.onCancel(dialogInterface)
        newItemFragmentListener.onCancel()
    }


    /**
     * Assigns a new item a due date.
     */
    private fun setDueDate(view: View) {
        val datePicker: DatePicker = view.findViewById(R.id.date_picker)

        val calendar: Calendar = GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth)

        buttonDueDate.text = SimpleDateFormat("EE, MMM d", Locale.US).format(calendar.time)

        item.setDueDate(SimpleDateFormat("EE, MMM d", Locale.US).format(calendar.time))
        item.setDueDateInMillis(calendar.timeInMillis)
    }

    /**
     * Sets the listener for saving a new item as well as the listener for enabling the save button.
     */
    private fun setListeners(title: EditText, save: Button, dueDate: Button, reminder: Button) {
        title.addTextChangedListener( object : TextWatcher {
            /**
             * Not necessary for this fragment.
             */
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            /**
             * Calls when the EditText responsible for getting a new item title is changed.
             */
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (title.text.toString().trim().isNotEmpty()) {
                    save.isEnabled = ENABLED
                    save.setTextColor(Color.parseColor(listColor))
                } else {
                    save.isEnabled = DISABLED
                    context?.let { ContextCompat.getColor(it, R.color.colorSecondaryVariant) }?.let {
                        save.setTextColor(it)
                    }
                }
            }

            /**
             * Not necessary for this fragment.
             */
            override fun afterTextChanged(p0: Editable?) {}
        })

        save.setOnClickListener {
            item.setTitle(title.text.toString().trim())
            newItemFragmentListener.onSave(item)
            dismiss()
        }

        dueDate.setOnClickListener {
            showDatePickerDialog(view)
        }

        reminder.setOnClickListener {
            showTimePickerDialog(view)
        }
    }

    /**
     * Sets a new item a reminder.
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

        buttonReminder.text = "$hourString:$minuteString $period"

        item.setReminder("$hourString:$minuteString $period")
        item.setReminderHour(hour)
        item.setReminderMinute(minute)
        item.setReminderId(((Date().time / 1000L) % Int.MAX_VALUE).toInt())
    }

    /**
     * Shows a DatePicker dialog for assigning a due date.
     */
    private fun showDatePickerDialog(view: View?) {
        val dialogDatePicker: View = View.inflate(activity, R.layout.dialog_date_picker, null)

        ActivityHelper.hideKeyboard(context, view)

        val builder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.DialogTheme)
            .setView(dialogDatePicker)
            .setPositiveButton(R.string.action_set) {_, _ ->
                setDueDate(dialogDatePicker)
                ActivityHelper.showKeyboard(context, InputMethodManager.SHOW_FORCED)
            }
            .setNegativeButton(R.string.action_cancel) {dialogInterface, _ ->
                dialogInterface.cancel()
                ActivityHelper.showKeyboard(context, InputMethodManager.SHOW_FORCED)
            }
            .setNeutralButton(R.string.action_clear) { _, _ ->
                item.clearDueDate()
                buttonDueDate.text = resources.getString(R.string.title_due_date)
                ActivityHelper.showKeyboard(context, InputMethodManager.SHOW_FORCED)
            }

        builder.show()
    }

    /**
     * Shows a TimePicker dialog for setting a reminder.
     */
    private fun showTimePickerDialog(view: View?) {
        val dialogTimePicker: View = View.inflate(activity, R.layout.dialog_time_picker, null)

        ActivityHelper.hideKeyboard(context, view)

        val builder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.DialogTheme)
            .setView(dialogTimePicker)
            .setPositiveButton(R.string.action_set) {_, _ ->
                setReminder(dialogTimePicker)
                ActivityHelper.showKeyboard(context, InputMethodManager.SHOW_FORCED)
            }
            .setNegativeButton(R.string.action_cancel) {dialogInterface, _ ->
                dialogInterface.cancel()
                ActivityHelper.showKeyboard(context, InputMethodManager.SHOW_FORCED)
            }
            .setNeutralButton(R.string.action_clear) { _, _ ->
                item.clearReminder()
                buttonReminder.text = resources.getString(R.string.title_reminder)
                ActivityHelper.showKeyboard(context, InputMethodManager.SHOW_FORCED)
            }

        builder.show()
    }

    interface NewItemFragmentListener {
        /**
         * Calls upon the fragment closing.
         */
        fun onCancel()

        /**
         * Calls when a new item is created.
         */
        fun onSave(item: Item)
    }
}