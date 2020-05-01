package com.teampower.cicerone

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.android.synthetic.main.content_scrolling.*

const val MY_PERMISSIONS_REQUEST_LOCATION_ID = 99
const val CHANNEL_ID = "CiceroneComms1337"
const val TAG = "Cicerone"
const val TAG_GEO = "Geofencer"

class MainActivity : AppCompatActivity() {
    private val latCon = LocationController()
    private val notCon = NotificationsController()
    private val geoCon = GeofencingController()
    private val dataCon = DataController()
    @RequiresApi(Build.VERSION_CODES.Q)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)

        user_location.text = getString(R.string.user_position, "-", "-")

        // Setup location services
        latCon.startLocation(this, this@MainActivity, user_location)

        // Setup geofencing services
        geoCon.startGeofencing(this)

        // Setup notifications
        notCon.createNotificationChannel(this)

        // Setup fab to test notifications
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Notification sent", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            notCon.sendNotification(this, "Test notification", "Hi, I'm the notification that was sent", 1)
        }

        // Make data request to API and display data
        dataCon.requestData("38.8897,-77.0089", venue_description)
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
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        latCon.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
