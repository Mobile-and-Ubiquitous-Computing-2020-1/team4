package com.teampower.cicerone

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.Geofence
import com.google.android.material.snackbar.Snackbar
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
    private val wikiManager by lazy { WikiInfoManager() }
    @RequiresApi(Build.VERSION_CODES.Q)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)

        // Test Wikipedia API. This variable is null if no page was found. TODO: Implement stuff to make the data come here so we can use it for displaying. Async problem
        val placeInfo = wikiManager.getPlaceInfo("Gyeongbokgung Palace")
        //wikipedia_extract.text = placeInfo?.extract
        user_location.text = getString(R.string.user_position, "-", "-")

        // Setup location services
        latCon.startLocation(this, this@MainActivity, user_location)

        // Setup geofencing services
        geoCon.startGeofencing(this)
        // TODO Example of adding multiple geofences. Should be moved to onResponse function
        // TODO for POI queries
        /*val pois = arrayOf(POI(37.4553, -122.1462, "POI 1"), POI(37.4654, -122.1609, "POI 2"))
        for (poi in pois) {
            val gf = geoCon.createGeofence(
                poi.lat,
                poi.long,
                poi.id,
                200F,
                Geofence.NEVER_EXPIRE,
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
            )
            geoCon.addGeofence(gf, this, poi)
        }
        geoCon.removeGeofence("POI 2", this)*/

        // Setup notifications
        notCon.createNotificationChannel(this)

        // Setup fab to test notifications
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Notification sent", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            notCon.sendNotificationToMain(
                this,
                "Test notification",
                "Hi, I'm the notification that was sent",
                1
            )
        }

        // Get last location and use it to make data request to API, then display the retrieved data
        // var curr_location = "38.8897,-77.0089"
        GlobalScope.launch {
            var curr_location: android.location.Location = latCon.getLocation()
            Log.d(TAG, "Current location: ${curr_location}. Requesting data...")
            dataCon.requestData(curr_location, venue_description, this@MainActivity)
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
