package com.teampower.cicerone

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.android.synthetic.main.content_scrolling.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex


const val MY_PERMISSIONS_REQUEST_LOCATION_ID = 99
const val CHANNEL_ID = "CiceroneComms1337"
const val TAG = "Cicerone"
const val TAG_GEO = "Geofencer"

// Use these to keep track of when the last recommendation has been set globally
// We can limit the number of notifications sent to the user with the timeBetweenRecommendations interval (in ms)
var mutex = Mutex()
var lastRecommendationTime = 0L
var timeBetweenRecommendations = 60000

class MainActivity : AppCompatActivity() {
    private val latCon = LocationController()
    private val notCon = NotificationsController()
    private val geoCon = GeofencingController()
    private val dataCon = DataController(geoCon)
    private lateinit var poiHistoryViewModel: POIHistoryViewModel
    private lateinit var poiSavedViewModel: POISavedViewModel
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
            val convertedPOI = poiSavedViewModel.convertPOIDataToPOI(poi)
            Log.d(TAG, "Triggered intent ${poi.name}")
            startActivity(GeofenceTriggeredActivity.getStartIntent(this, toJson(convertedPOI)))
        }
        val onListItemStarClicked: (poi: POIData, holder: POIListAdapter.POIViewHolder) -> Unit =
            { poi, holder ->
                val convertedPOI = poiSavedViewModel.convertPOIDataToPOI(poi)
                MainScope().launch {
                    poiSavedViewModel.toggleFavorite(
                        convertedPOI,
                        applicationContext,
                        holder.starImage
                    )
                }
            }

        poiHistoryViewModel = ViewModelProvider(this).get(POIHistoryViewModel::class.java)
        poiSavedViewModel = ViewModelProvider(this).get(POISavedViewModel::class.java)

        // List history of recent POIs
        val historyRecyclerView = findViewById<RecyclerView>(R.id.historyrecyclerview)
        val historyAdapter =
            POIListAdapter(this, onListItemInfoClicked, onListItemStarClicked, poiSavedViewModel)
        historyRecyclerView.adapter = historyAdapter
        historyRecyclerView.layoutManager = LinearLayoutManager(this)

        poiHistoryViewModel.recentSavedPOIs.observe(this, Observer { pois ->
            // Update the cached copy of the words in the adapter.
            pois?.let { historyAdapter.setPOIs(it) }
            // Set POI history in DataController to filter out previous POIs
            dataCon.setPOIHistory(pois)
        })

        // List saved POIs
        val savedRecyclerView = findViewById<RecyclerView>(R.id.savedrecyclerview)
        val savedAdapter =
            POIListAdapter(this, onListItemInfoClicked, onListItemStarClicked, poiSavedViewModel)
        savedRecyclerView.adapter = savedAdapter
        savedRecyclerView.layoutManager = LinearLayoutManager(this)

        poiSavedViewModel.recentSavedPOIs.observe(this, Observer { pois ->
            // Update the cached copy of the words in the adapter.
            pois?.let { savedAdapter.setPOIs(it) }
        })

        // Connect to local table of category scores - have to do this here since datacon is not a viewmodel provider
        catViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        catViewModel.allCat.observe(this, Observer { cats ->
            // Set table of scores in DataCon
            dataCon.setCategoryScores(cats)
        })
        dataCon.setCatViewModel(catViewModel)

        // Setup location services
        latCon.startLocation(this, this@MainActivity, dataCon)

        // Setup geofencing services
        geoCon.startGeofencing(this)

        // Setup notifications
        notCon.createNotificationChannel(this)

        seeAllSavedSpotsBtn.setOnClickListener {
            startActivity(Intent(this, ListSavedPOIActivity()::class.java))
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
/*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }*/

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
