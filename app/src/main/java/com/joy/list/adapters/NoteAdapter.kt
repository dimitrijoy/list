package com.joy.list.adapters

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import com.joy.list.R
import com.joy.list.utils.Note

import java.text.SimpleDateFormat
import java.util.*

/**
 * Handles notes within a RecyclerView.
 */
class NoteAdapter(private val onNoteListener: OnNoteListener) : RecyclerView.Adapter<NoteAdapter.NoteHolder>() {
    private var notes: List<Note> = emptyList()

    /**
     * Initializes the adapter's view holder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteAdapter.NoteHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteHolder(itemView, onNoteListener)
    }

    /**
     * Displays the title, due date, and reminder of a note in a itemView.
     */
    override fun onBindViewHolder(holder: NoteAdapter.NoteHolder, position: Int) {
        val note: Note = notes[position]


        holder.textNoteText.text = note.getText()

        showDueDate(note, holder)
        showReminder(note, holder)
        showTitle(note, holder)
    }

    /**
     * Gets the number of notes.
     */
    override fun getItemCount() = notes.size

    /**
     * Sets the notes in NoteAdapter.
     */
    fun setNotes(notes: List<Note>) {
        this.notes = notes
        notifyDataSetChanged()
    }

    /**
     * Shows the due date of a note if not null.
     */
    private fun showDueDate(note: Note, holder: NoteHolder) {
        if (note.getDueDate() == null) {
            holder.linearDueDate.visibility = View.GONE
        } else {
            holder.linearDueDate.visibility = View.VISIBLE

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = note.getDueDateInMillis()!!

            val daysSinceEpoch = (calendar.get(Calendar.YEAR) - 1970) * 365
            val daysSinceEpochCurrent = (Calendar.getInstance().get(Calendar.YEAR) - 1970) * 365

            val dayOfYear: Int = SimpleDateFormat("D", Locale.US).format(Calendar.getInstance().time).toInt() + daysSinceEpochCurrent
            val dueDate: String = when (calendar.get(Calendar.DAY_OF_YEAR) + daysSinceEpoch) {
                dayOfYear - 1 -> "Yesterday"
                dayOfYear -> "Today"
                dayOfYear + 1 -> "Tomorrow"
                else -> note.getDueDate()!!
            }

            if (calendar.get(Calendar.DAY_OF_YEAR) + daysSinceEpoch < dayOfYear) {
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
     * Shows the reminder of a note if not null.
     */
    private fun showReminder(note: Note, holder: NoteHolder) {
        if (note.getReminder() == null) {
            holder.linearReminder.visibility = View.GONE
        } else {
            holder.linearReminder.visibility = View.VISIBLE
            holder.textReminder.text = note.getReminder()
        }
    }

    /**
     * Shows the title of a note if not null.
     */
    private fun showTitle(note: Note, holder: NoteHolder) {
        if (note.getTitle().isNullOrEmpty()) {
            holder.textNoteTitle.visibility = View.GONE
        } else {
            holder.textNoteTitle.visibility = View.VISIBLE
            holder.textNoteTitle.text = note.getTitle()
        }
    }

    /**
     * Implements the listeners for notes.
     */
    interface OnNoteListener {
        /**
         * Calls when a note is clicked.
         */
        fun onNoteClick(note: Note)
    }

    /**
     * Holds the views of noteView for each note. Sets the note listener.
     */
    inner class NoteHolder(itemView: View, listener: OnNoteListener) : RecyclerView.ViewHolder(itemView) {
        val imageCalendar: ImageView = itemView.findViewById(R.id.image_calendar)
        val linearDueDate: LinearLayout = itemView.findViewById(R.id.linear_due_date)
        val linearReminder: LinearLayout = itemView.findViewById(R.id.linear_reminder)
        val textDueDate : TextView = itemView.findViewById(R.id.text_due_date)
        val textNoteText : TextView = itemView.findViewById(R.id.text_note_text)
        val textNoteTitle : TextView = itemView.findViewById(R.id.text_note_title)
        val textReminder : TextView = itemView.findViewById(R.id.text_reminder)

        private val onNoteListener: OnNoteListener = listener

        init {
            itemView.setOnClickListener {
                onNoteListener.onNoteClick(notes[absoluteAdapterPosition])
            }

        }
    }
}