package com.joy.list.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.gson.Gson

import com.joy.list.adapters.ListAdapter
import com.joy.list.models.ListViewModel
import com.joy.list.R
import com.joy.list.utils.ActivityHelper
import com.joy.list.utils.List

/**
 * Displays a user's lists and the quick access pane. Allows for creating new lists as well.
 */
class MainActivity : AppCompatActivity(), ListAdapter.OnListListener, View.OnClickListener {
    private val listAdapter = ListAdapter(this)
    private lateinit var listViewModel: ListViewModel

    private lateinit var listColors: Array<String?>
    private var listColorIndex: Int = INIT_LIST_COLOR_INDEX

    /**
     * Stores the constant, static components of the activity, so to speak.
     */
    companion object {
        const val EXTRA_LIST: String = "com.joy.activities.MainActivity.EXTRA_LIST"
        const val INIT_LIST_COLOR_INDEX: Int = 0

        /**
         * Gets the intent for starting an activity from the quick access pane or SearchActivity.
         */
        fun getStartIntent(context: Context, destination: Class<*>): Intent {
            return Intent(context, destination)
        }

        /**
         * Gets the intent for starting ListActivity.
         */
        fun getStartIntent(context: Context, destination: Class<*>, list: List): Intent {
            val intent = Intent(context, destination)
            intent.putExtra(EXTRA_LIST, Gson().toJson(list))
            return intent
        }
    }

    /**
     * Initializes the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        populateLists()
        setListColors()
        setOnClickListeners()
    }

    /**
     * Opens an activity from the quick access pane.
     */
    override fun onClick(view: View) {
        var intent = Intent()
        when (view.id) {
            R.id.card_favorites -> intent = getStartIntent(this, FavoritesActivity::class.java)
            R.id.card_notes -> intent = getStartIntent(this, NotesActivity::class.java)
            R.id.card_upcoming -> intent = getStartIntent(this, UpcomingActivity::class.java)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    /**
     * Opens a clicked list. Starts ListActivity.
     */
    override fun onListClick(list: List) {
        startActivity(getStartIntent(this, ListActivity::class.java, list))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    /**
     * Shows a dialog for creating a new list.
     */
    fun showNewListDialog(view: View) {
        val dialogNewList: View = View.inflate(this, R.layout.dialog_new_edit_list, null)
        val editListNewTitle: EditText = dialogNewList.findViewById(R.id.edit_new_list_title)
        editListNewTitle.setOnFocusChangeListener { _, _ ->
            editListNewTitle.post {
                ActivityHelper.showKeyboard(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        editListNewTitle.requestFocus()

        setOnCheckedListColorListener(dialogNewList)

        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle(R.string.title_new_list)
            .setView(dialogNewList)
            .setPositiveButton(R.string.action_create) { _, _ ->
                insertList(editListNewTitle.text.toString().trim(), listColors[listColorIndex])
            }
            .setNegativeButton(R.string.action_cancel) { dialogInterface, _ ->
                dialogInterface.cancel()
            }

        builder.show()
    }

    /**
     * Starts the activity for searching items.
     */
    fun startSearchActivity(view: View) {
        startActivity(getStartIntent(this, SearchActivity::class.java))
    }

    /**
     * Inserts a list into the database.
     */
    private fun insertList(listTitle: String, listColor: String?) {
        val list = List()
        if (listTitle.isNotEmpty()) list.setTitle(listTitle)
        list.setColor(listColor)

        listViewModel.insertList(list)
    }

    /**
     * Populates the user's lists.
     */
    private fun populateLists() {
        val recyclerLists: RecyclerView = findViewById(R.id.recycler_lists)
        recyclerLists.layoutManager = LinearLayoutManager(this)
        recyclerLists.setHasFixedSize(false)
        recyclerLists.adapter = listAdapter

        listViewModel = ViewModelProvider(this, ViewModelProvider
            .AndroidViewModelFactory.getInstance(application))
            .get(ListViewModel::class.java)
        listViewModel.getAllLists().observe(this, Observer { lists ->
            listAdapter.setLists(lists)
        })
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
    private fun setOnCheckedListColorListener(view: View) {
        var previousCheckedId: Int = R.id.radio_default_list_color

        val radioListColor: RadioGroup = view.findViewById(R.id.radio_list_color)
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
     * Sets the quick access pane listeners.
     */
    private fun setOnClickListeners() {
        val cardFavorites: CardView = findViewById(R.id.card_favorites)
        cardFavorites.setOnClickListener(this)

        val cardNotes: CardView = findViewById(R.id.card_notes)
        cardNotes.setOnClickListener(this)

        val cardUpcoming: CardView = findViewById(R.id.card_upcoming)
        cardUpcoming.setOnClickListener(this)
    }

}