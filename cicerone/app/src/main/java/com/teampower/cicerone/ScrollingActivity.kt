package com.teampower.cicerone

import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_scrolling.*
import java.util.jar.Manifest


class ScrollingActivity : AppCompatActivity() {
    private val TAG = "Cicerone"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var requestingLocationUpdates = true
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)


        fab.setOnClickListener { view ->
            Snackbar.make(view, "Fun stuff here", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()}

        // Setup location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = createLocationRequest()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    Log.v(TAG, location.toString())
                }
            }
        }

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

    private fun createLocationRequest(): LocationRequest {
        // https://developer.android.com/training/location/change-location-settings
        Log.v(TAG, "Setting up location services")
        val locationRequest = LocationRequest.create().apply{
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        // Get current location settings
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            Log.v(TAG, "All permissions OK. Enabling Location Updates")
            requestingLocationUpdates = true
        }

        task.addOnFailureListener { exception ->
            Log.v(TAG, "Permissions failed")
//            if (exception is ResolvableApiException){
//                try{
//                    // Show dialog for user to allow location settings
//                    exception.startResolutionForResult(this@Main, REQUEST_CHECK_SETTINGS)
//                } catch (sendEx: IntentSender.SendIntentException){
//                    // Ignore error
//                }
//            }

        }
        return locationRequest
    }

    override fun onResume() {
        super.onResume()
        Log.v(TAG, "Activity resumed")
        if ( requestingLocationUpdates ) {
            Log.v(TAG, "Starting location updates")
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        Log.v(TAG, "Location updates started")
    }
}
