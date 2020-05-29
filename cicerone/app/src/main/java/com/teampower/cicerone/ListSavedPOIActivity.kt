package com.teampower.cicerone

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.teampower.cicerone.adapters.POIListAdapter
import com.teampower.cicerone.database.POIData
import com.teampower.cicerone.database.history_table.POISavedViewModel
import com.teampower.cicerone.wikipedia.WikipediaPlaceInfo
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

        // Adapter on click functions
        val onListItemInfoClicked: (poi: POIData) -> Unit = { poi ->
            val convertedPOI = POI(
                poi.foursquareID,
                poi.name,
                poi.category,
                poi.latitude,
                poi.longitude,
                poi.description,
                poi.distance,
                poi.address,
                poi.wikipediaInfoJSON?.let { MainActivity.fromJson<WikipediaPlaceInfo>(it) })
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
                Log.d(TAG, "STARRING ${poi.name}")
                DrawableCompat.setTint(
                    DrawableCompat.wrap(holder.starImage.drawable),
                    ContextCompat.getColor(this, R.color.yellow)
                )
            }

        // List saved POIs
        val savedRecyclerView = findViewById<RecyclerView>(R.id.allSavedPOIRecyclerView)
        val savedAdapter = POIListAdapter(this, onListItemInfoClicked, onListItemStarClicked)
        savedRecyclerView.adapter = savedAdapter
        savedRecyclerView.layoutManager = LinearLayoutManager(this)

        poiSavedViewModel = ViewModelProvider(this).get(POISavedViewModel::class.java)
        poiSavedViewModel.allPOI.observe(this, Observer { pois ->
            // Update the cached copy of the words in the adapter.
            pois?.let { savedAdapter.setPOIs(it) }
        })

    }
}
