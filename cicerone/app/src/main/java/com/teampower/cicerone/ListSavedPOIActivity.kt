package com.teampower.cicerone

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.teampower.cicerone.adapters.POIListAdapter
import com.teampower.cicerone.database.POIData
import com.teampower.cicerone.viewmodels.POISavedViewModel
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ListSavedPOIActivity : AppCompatActivity() {
    private val TRIG_TAG = "ListSavedPOIActivity"
    private lateinit var poiViewModel: POISavedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_pois)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Home button to MainActivity

        poiViewModel = ViewModelProvider(this).get(POISavedViewModel::class.java)

        // Adapter on click functions
        val onListItemInfoClicked: (poi: POIData) -> Unit = { poi ->
            val convertedPOI = poiViewModel.convertPOIDataToPOI(poi)
            Log.d(TAG, "Triggered intent ${poi.name}")
            startActivity(
                GeofenceTriggeredActivity.getStartIntent(
                    this,
                    MainActivity.toJson(convertedPOI)
                )
            )
        }
        val onListItemStarClicked: (poi: POIData, holder: POIListAdapter.POIViewHolder) -> Unit =
            { poi, holder ->
                val convertedPOI = poiViewModel.convertPOIDataToPOI(poi)
                MainScope().launch {
                    poiViewModel.toggleFavorite(convertedPOI, applicationContext, holder.starImage)
                }
            }

        // List saved POIs
        val savedRecyclerView = findViewById<RecyclerView>(R.id.allSavedPOIRecyclerView)
        val savedAdapter =
            POIListAdapter(this, onListItemInfoClicked, onListItemStarClicked, poiViewModel)
        savedRecyclerView.adapter = savedAdapter
        savedRecyclerView.layoutManager = LinearLayoutManager(this)

        poiViewModel.allPOI.observe(this, Observer { pois ->
            // Update the cached copy of the words in the adapter.
            pois?.let { savedAdapter.setPOIs(it) }
        })

    }
}
