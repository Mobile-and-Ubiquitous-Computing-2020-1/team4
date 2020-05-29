package com.teampower.cicerone

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jakewharton.threetenabp.AndroidThreeTen
import com.teampower.cicerone.adapters.POIListAdapter
import com.teampower.cicerone.control.DataController
import com.teampower.cicerone.control.GeofencingController
import com.teampower.cicerone.control.LocationController
import com.teampower.cicerone.control.NotificationsController
import com.teampower.cicerone.database.POIData
import com.teampower.cicerone.viewmodels.CategoryViewModel
import com.teampower.cicerone.viewmodels.POIHistoryViewModel
import com.teampower.cicerone.viewmodels.POISavedViewModel
import com.teampower.cicerone.wikipedia.WikipediaPlaceInfo
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.android.synthetic.main.content_scrolling.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


const val MY_PERMISSIONS_REQUEST_LOCATION_ID = 99
const val CHANNEL_ID = "CiceroneComms1337"
const val TAG = "Cicerone"
const val TAG_GEO = "Geofencer"

class MainActivity : AppCompatActivity() {
    private val latCon = LocationController()
    private val notCon = NotificationsController()
    private val geoCon = GeofencingController()
    private val dataCon = DataController(geoCon)
    private lateinit var poiHistoryViewModel: POIHistoryViewModel
    private lateinit var poiViewModel: POISavedViewModel
    private lateinit var catViewModel: CategoryViewModel

    companion object {
        inline fun <reified T> fromJson(json: String): T {
            return Gson().fromJson(json, object : TypeToken<T>() {}.type)
        }

        inline fun <reified T> toJson(input: T): String {
            return Gson().toJson(input)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)
        AndroidThreeTen.init(this)

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
                poi.wikipediaInfoJSON?.let { fromJson<WikipediaPlaceInfo>(it) })
            Log.d(TAG, "Triggered intent ${poi.name}")
            startActivity(GeofenceTriggeredActivity.getStartIntent(this, toJson(convertedPOI)))
        }
        val onListItemStarClicked: (poi: POIData, holder: POIListAdapter.POIViewHolder) -> Unit =
            { poi, holder ->
                Log.d(TAG, "STARRING ${poi.name}")
                DrawableCompat.setTint(
                    DrawableCompat.wrap(holder.starImage.drawable),
                    ContextCompat.getColor(this, R.color.yellow)
                )
            }

        // List history of recent POIs
        val historyRecyclerView = findViewById<RecyclerView>(R.id.historyrecyclerview)
        val historyAdapter =
            POIListAdapter(this, onListItemInfoClicked, onListItemStarClicked)
        historyRecyclerView.adapter = historyAdapter
        historyRecyclerView.layoutManager = LinearLayoutManager(this)

        poiHistoryViewModel = ViewModelProvider(this).get(POIHistoryViewModel::class.java)
        poiHistoryViewModel.allPOI.observe(this, Observer { pois ->
            // Update the cached copy of the words in the adapter.
            pois?.let { historyAdapter.setPOIs(it) }
        })

        // List saved POIs
        val savedRecyclerView = findViewById<RecyclerView>(R.id.savedrecyclerview)
        val savedAdapter = POIListAdapter(this, onListItemInfoClicked, onListItemStarClicked)
        savedRecyclerView.adapter = savedAdapter
        savedRecyclerView.layoutManager = LinearLayoutManager(this)

        poiViewModel = ViewModelProvider(this).get(POISavedViewModel::class.java)
        poiViewModel.recentSavedPOIs.observe(this, Observer { pois ->
            // Update the cached copy of the words in the adapter.
            pois?.let { savedAdapter.setPOIs(it) }
        })

        // Connect to local table of category scores
        catViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        catViewModel.allCat.observe(this, Observer { cats ->
            // Set table of scores in DataCon
            dataCon.setCategoryScores(cats)
        })


        // Setup location services
        latCon.startLocation(this, this@MainActivity, dataCon)

        // Setup geofencing services
        geoCon.startGeofencing(this)

        // Setup notifications
        notCon.createNotificationChannel(this)

        see_all_saved_poi_btn.setOnClickListener {
            startActivity(Intent(this, ListSavedPOIActivity()::class.java))
        }

        // Get last location and use it to make data request to API, then display the retrieved data
        // var curr_location = "38.8897,-77.0089"
        GlobalScope.launch {
            var currLocation: android.location.Location = latCon.getLocation()
            Log.d(TAG, "Current location: ${currLocation}. Requesting data...")
            dataCon.requestData(currLocation, venue_description, this@MainActivity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        Log.v(TAG, "Activity resumed")
        latCon.enableLocationTracking(this, this@MainActivity)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    // TODO Refactor this
    //  Define a wrapper function for the onRequestPermissionsResults in LocationController
    //  to properly override the function. Not ideal, but I didn't manage to get this to work
    //  outside the MainActivity.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        latCon.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
