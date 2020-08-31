package com.joy.list.activities

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson

import com.joy.list.R
import com.joy.list.adapters.NoteAdapter
import com.joy.list.models.NoteViewModel
import com.joy.list.utils.ActivityHelper
import com.joy.list.utils.Note

/**
 * Displays the items of a particular list. Provides additional functionality, such as completing,
 * deleting, editing, and favoriting items.
 */
class NotesActivity : AppCompatActivity(), NoteAdapter.OnNoteListener {
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var noteViewModel: NoteViewModel

    private lateinit var fab: FloatingActionButton

    /**
     * Stores the constant, static components of the activity, so to speak.
     */
    companion object {
        const val NOTES_TITLE = "Notes"
        const val NOTES_COLOR = "#ffbb33"

        const val EXTRA_INSERT_NOTE: String = "com.joy.activities.NotesActivity.EXTRA_INSERT_NOTE"
        const val EXTRA_NOTE: String = "com.joy.activities.NotesActivity.EXTRA_NOTE"
        const val EXTRA_FROM_REMINDER: String = "com.joy.activities.NotesActivity.EXTRA_FROM_REMINDER"

        /**
         * Gets the intent for starting ListActivity.
         */
        fun getStartIntent(context: Context, destination: Class<*>, note: Note, insertNote: Boolean): Intent {
            val intent = Intent(context, destination)
            intent.putExtra(EXTRA_NOTE, Gson().toJson(note))
            intent.putExtra(EXTRA_INSERT_NOTE, Gson().toJson(insertNote))
            return intent
        }

        /**
         * Gets the intent for starting ListActivity.
         */
        fun getStartIntent(context: Context, destination: Class<*>, note: Note, insertNote: Boolean, fromReminder: Boolean): Intent {
            val intent = Intent(context, destination)
            intent.putExtra(EXTRA_NOTE, Gson().toJson(note))
            intent.putExtra(EXTRA_INSERT_NOTE, Gson().toJson(insertNote))
            intent.putExtra(EXTRA_FROM_REMINDER, Gson().toJson(fromReminder))
            return intent
        }
    }

    /**
     * Initializes the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        fab = findViewById(R.id.fab)
        fab.backgroundTintList = ColorStateList.valueOf(Color.parseColor((NOTES_COLOR)))

        ActivityHelper.setStatusBarColor(this, R.color.colorPrimary)
        ActivityHelper.setSupportActionBar(this)
        ActivityHelper.setToolbarColor(this, R.color.colorPrimary)

        initializeViewModels()
        populateNotes()
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
     * Opens a clicked note. Starts NoteActivity.
     */
    override fun onNoteClick(note: Note) {
        startActivity(getStartIntent(this, NoteActivity::class.java, note, false))
    }

    /**
     * Inserts a note into the database.
     */
    fun insertNote(view: View) {
        startActivity(getStartIntent(this, NoteActivity::class.java, Note(), true))
    }

    /**
     * Initializes the activity's view models.
     */
    private fun initializeViewModels() {
        noteViewModel = ViewModelProvider(this, ViewModelProvider
            .AndroidViewModelFactory.getInstance(application))
            .get(NoteViewModel::class.java)
    }

    /**
     * Populates the user's notes.
     */
    private fun populateNotes() {
        noteAdapter = NoteAdapter(this)

        val recyclerNotes: RecyclerView = findViewById(R.id.recycler_notes)
        recyclerNotes.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerNotes.setHasFixedSize(false)
        recyclerNotes.adapter = noteAdapter

        noteViewModel.getAllNotes().observe(this, Observer { notes ->
            noteAdapter.setNotes(notes)
        })
    }

    /**
     * Hides the floating action button on scroll down. Shows the floating action button otherwise.
     */
    private fun setOnScrollListener() {
        val nestedScrollView: NestedScrollView = findViewById(R.id.nested_scroll_view)
        nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY)
                fab.hide()
            else
                fab.show()
        }
    }
}

