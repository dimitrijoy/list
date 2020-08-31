package com.joy.list.activities

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.SearchView

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.joy.list.R
import com.joy.list.adapters.SearchAdapter
import com.joy.list.models.ItemViewModel
import com.joy.list.models.ListViewModel
import com.joy.list.utils.ActivityHelper
import com.joy.list.utils.Item
import com.joy.list.utils.List

/**
 * Displays the search results pertaining to a particular query provided by the user. Search
 * results only support items currently.
 */
class SearchActivity : AppCompatActivity(), SearchAdapter.OnSearchListener, SearchView.OnQueryTextListener {
    private lateinit var listViewModel: ListViewModel

    private lateinit var itemViewModel: ItemViewModel

    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchView: SearchView

    /**
     * Stores the constant, static components of the activity, so to speak.
     */
    companion object {
        private const val EMPTY_QUERY = ""
    }

    /**
     * Initializes the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        ActivityHelper.setStatusBarColor(this, R.color.colorSecondary)
        ActivityHelper.setSupportActionBar(this)
        ActivityHelper.setToolbarColor(this, R.color.colorSecondary)

        initializeAdapter()
        initializeViewModels()
        setSearchView()
    }

    /**
     * Resets the search bar.
     */
    override fun onResume() {
        super.onResume()
        resetSearchView()
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
     * Refreshes search results on query text change.
     */
    override fun onQueryTextChange(query: String?): Boolean {
        populateSearchResults(query)
        return false
    }

    /**
     * Not necessary in this activity.
     */
    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    /**
     * Opens a clicked search result.
     */
    override fun onSearchResultClick(searchResult: Item) {
        startActivity(ListActivity.getStartIntent(this, ItemActivity::class.java, listViewModel.getList(searchResult.getListId()), searchResult))
    }

    /**
     * Initializes the activity's adapter.
     */
    private fun initializeAdapter() {
        searchAdapter = SearchAdapter(this)

        val recyclerSearchResults: RecyclerView = findViewById(R.id.recycler_search_results)
        recyclerSearchResults.layoutManager = LinearLayoutManager(this)
        recyclerSearchResults.setHasFixedSize(false)
        recyclerSearchResults.adapter = searchAdapter
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
     * Populates the search results given a query.
     */
    private fun populateSearchResults(query: String?) {
        val temp = "%$query%"
        itemViewModel.getAllItemsThatContain(temp).observe(this, Observer { searchResults ->
            val lists: MutableList<List> = arrayListOf()
            for (searchResult in searchResults) {
                lists.add(listViewModel.getList(searchResult.getListId()))
            }

            searchAdapter.setLists(lists)
            searchAdapter.setSearchResults(searchResults)
        })
    }

    /**
     * Resets the search bar within the layout of the activity.
     */
    private fun resetSearchView() {
        populateSearchResults(EMPTY_QUERY)
        searchView.setQuery(EMPTY_QUERY, true)
    }

    /**
     * Sets the search bar within the layout of the activity.
     */
    private fun setSearchView() {
        searchView = findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(this)
        searchView.isIconifiedByDefault = false
        searchView.requestFocus()

        val searchPlate: View = searchView.findViewById(searchView.context.resources.getIdentifier("android:id/search_plate", null, null))
        searchPlate.background.setColorFilter(ContextCompat.getColor(this, R.color.colorSecondary), PorterDuff.Mode.MULTIPLY)

        val close: ImageView = searchView.findViewById(searchView.context.resources.getIdentifier("android:id/search_close_btn", null, null))
        close.isEnabled = false
        close.setImageDrawable(null)
    }
}