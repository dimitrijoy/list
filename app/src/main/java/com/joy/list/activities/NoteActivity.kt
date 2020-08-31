package com.joy.list.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.gson.Gson
import com.joy.list.R
import com.joy.list.models.NoteViewModel
import com.joy.list.utils.ActivityHelper
import com.joy.list.utils.List
import com.joy.list.utils.Note
import java.text.SimpleDateFormat
import java.util.*


class NoteActivity : AppCompatActivity() {
    private lateinit var list: List

    private var deleteOnInsert: Boolean = false
    private var insertNote: Boolean = false
    private lateinit var note: Note
    private lateinit var noteViewModel: NoteViewModel

    /**
     * Initializes the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        insertNote = Gson().fromJson<Boolean>(intent.getStringExtra(NotesActivity.EXTRA_INSERT_NOTE), Boolean::class.java)
        note = Gson().fromJson<Note>(intent.getStringExtra(NotesActivity.EXTRA_NOTE), Note::class.java)

        try {
            val fromReminder: Boolean = Gson().fromJson<Boolean>(intent.getStringExtra(NotesActivity.EXTRA_FROM_REMINDER), Boolean::class.java)
            if (fromReminder)
                ActivityHelper.dismissReminder(this, note)
        } catch (e: IllegalStateException) {
            Log.e("error!", "IllegalStateException: Boolean extra 'fromReminder' cannot be null (it can and, in fact, usually will).")
        }

        ActivityHelper.setStatusBarColor(this, R.color.colorSecondary)
        ActivityHelper.setSupportActionBar(this)
        ActivityHelper.setToolbarColor(this, R.color.colorSecondary)

        initializeViewModel()
        updateActivity()
    }

    /**
     * Finishes the activity. Saves the changes made to a note.
     */
    override fun finish() {
        super.finish()

        val editNoteText: EditText = findViewById(R.id.edit_note_text)
        if (editNoteText.text.toString().trim().isNotEmpty())
            note.setText(editNoteText.text.toString().trim())
        else
            note.setText(Note.EMPTY_NOTE)

        val editNoteTitle: EditText = findViewById(R.id.edit_note_title)
        note.setTitle(editNoteTitle.text.toString().trim())

        if (insertNote && !deleteOnInsert) {
            noteViewModel.insertNote(note)

            if (note.getReminderId() != null)
                ActivityHelper.setReminder(this, note)
        } else if (!insertNote) {
            noteViewModel.updateNote(note)

            if (note.getReminderId() != null)
                ActivityHelper.setReminder(this, note)
        }

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
     * Deletes a note with a confirmation dialog.
     */
    fun deleteNote(view: View?) {
        val dialogDeleteConfirmation: View = layoutInflater.inflate(R.layout.dialog_delete_confirmation, null)
        val textWarning: TextView = dialogDeleteConfirmation.findViewById(R.id.text_warning)
        textWarning.setText(R.string.msg_delete_note)

        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.DeleteConfirmationDialogTheme)
            .setTitle(R.string.title_delete_note)
            .setView(dialogDeleteConfirmation)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                deleteOnInsert = insertNote
                finish()
                noteViewModel.deleteNote(note)

                if (note.getReminderId() != null)
                    ActivityHelper.dismissReminder(this, note)
            }
            .setNegativeButton(R.string.action_cancel) { dialogInterface, _ ->
                dialogInterface.cancel()
            }

        builder.show()
    }

    /**
     * Shows a DatePicker dialog for assigning a due date.
     */
    fun showDatePickerDialog(view: View?) {
        val editNoteTitle: EditText = findViewById(R.id.edit_note_title)
        editNoteTitle.clearFocus()

        val dialogDatePicker: View = View.inflate(this, R.layout.dialog_date_picker, null)

        ActivityHelper.hideKeyboard(this, view)

        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.DialogTheme)
            .setView(dialogDatePicker)
            .setPositiveButton(R.string.action_set) {_, _ ->
                setDueDate(dialogDatePicker)
                setNoteDueDate()
            }
            .setNegativeButton(R.string.action_cancel) {dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .setNeutralButton(R.string.action_clear) {_, _ ->
                clearDueDate()
                setNoteDueDate()
            }

        builder.show()
    }

    /**
     * Shows a TimePicker dialog for setting a reminder.
     */
    fun showTimePickerDialog(view: View?) {
        val editNoteTitle: EditText = findViewById(R.id.edit_note_title)
        editNoteTitle.clearFocus()

        val dialogTimePicker: View = View.inflate(this, R.layout.dialog_time_picker, null)

        ActivityHelper.hideKeyboard(this, view)

        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.DialogTheme)
            .setView(dialogTimePicker)
            .setPositiveButton(R.string.action_set) {_, _ ->
                setReminder(dialogTimePicker)
                setNoteReminder()
            }
            .setNegativeButton(R.string.action_cancel) {dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .setNeutralButton(R.string.action_clear) {_, _ ->
                clearReminder()
                setNoteReminder()
            }

        builder.show()
    }

    /**
     * Clears the due date of a note.
     */
    private fun clearDueDate() {
        note.clearDueDate()
        noteViewModel.updateNote(note)
    }

    /**
     * Clears the reminder of a note.
     */
    private fun clearReminder() {
        if (note.getReminderId() != null)
            ActivityHelper.dismissReminder(this, note)

        note.clearReminder()
        noteViewModel.updateNote(note)
    }

    /**
     * Initializes the activity's view model.
     */
    private fun initializeViewModel() {
        noteViewModel = ViewModelProvider(this, ViewModelProvider
            .AndroidViewModelFactory.getInstance(application))
            .get(NoteViewModel::class.java)
    }

    /**
     * Assigns a note a due date.
     */
    private fun setDueDate(view: View) {
        val datePicker: DatePicker = view.findViewById(R.id.date_picker)

        val calendar: Calendar = GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth)

        note.setDueDate(SimpleDateFormat("EE, MMM d", Locale.US).format(calendar.time))
        note.setDueDateInMillis(calendar.timeInMillis)

        noteViewModel.updateNote(note)
    }

    /**
     * Sets the note due date within the layout of the activity.
     */
    private fun setNoteDueDate() {
        val buttonNoteDueDate: Button = findViewById(R.id.button_note_due_date)
        buttonNoteDueDate.text = note.getDueDate() ?: resources.getString(R.string.title_due_date)
    }

    /**
     * Sets the note reminder within the layout of the activity.
     */
    private fun setNoteReminder() {
        val buttonNoteReminder: Button = findViewById(R.id.button_note_reminder)
        buttonNoteReminder.text = note.getReminder() ?: resources.getString(R.string.title_reminder)
    }

    /**
     * Sets the note text within the layout of the activity.
     */
    private fun setNoteText() {
        val editNoteText: EditText = findViewById(R.id.edit_note_text)
        editNoteText.setText(note.getText())
        editNoteText.clearFocus()
    }

    /**
     * Sets the note title within the layout of the activity.
     */
    private fun setNoteTitle() {
        val editNoteTitle: EditText = findViewById(R.id.edit_note_title)
        editNoteTitle.setText(note.getTitle())
        editNoteTitle.imeOptions = EditorInfo.IME_ACTION_DONE
        editNoteTitle.setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        editNoteTitle.clearFocus()
    }

    /**
     * Sets a note a reminder.
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

        note.setReminder("$hourString:$minuteString $period")
        note.setReminderHour(hour)
        note.setReminderMinute(minute)
        note.setReminderId(((Date().time / 1000L) % Int.MAX_VALUE).toInt())

        noteViewModel.updateNote(note)
    }

    /**
     * Updates the activity with the most recent details of a particular note.
     */
    private fun updateActivity() {
        setNoteText()
        setNoteTitle()
        setNoteDueDate()
        setNoteReminder()
    }
}