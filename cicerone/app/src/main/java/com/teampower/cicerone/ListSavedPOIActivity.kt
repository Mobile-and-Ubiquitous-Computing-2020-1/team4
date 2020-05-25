package com.teampower.cicerone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.teampower.cicerone.database.history_table.POISavedListAdapter
import com.teampower.cicerone.database.history_table.POISavedViewModel
import kotlinx.android.synthetic.main.activity_scrolling.*


class ListSavedPOIActivity : AppCompatActivity() {
    private val TRIG_TAG = "ListSavedPOIActivity"
    private lateinit var poiSavedViewModel: POISavedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_pois)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Home button to MainActivity

        poiSavedViewModel = ViewModelProvider(this).get(POISavedViewModel::class.java)

        // List saved POIs
        val savedRecyclerView = findViewById<RecyclerView>(R.id.allSavedPOIRecyclerView)
        val savedAdapter = POISavedListAdapter(this)
        savedRecyclerView.adapter = savedAdapter
        savedRecyclerView.layoutManager = LinearLayoutManager(this)

        poiSavedViewModel = ViewModelProvider(this).get(POISavedViewModel::class.java)
        poiSavedViewModel.allPOI.observe(this, Observer { pois ->
            // Update the cached copy of the words in the adapter.
            pois?.let { savedAdapter.setPOIs(it) }
        })

    }
}
