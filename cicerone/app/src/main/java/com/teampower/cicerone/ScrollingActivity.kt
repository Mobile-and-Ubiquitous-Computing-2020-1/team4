package com.teampower.cicerone

import android.content.IntentSender
import android.location.Location
import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)
        // Setup location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fab.setOnClickListener { view -> onClickLocation(view, fusedLocationClient)}

        createLocationRequest()
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

    private fun onClickLocation(view:View, client: FusedLocationProviderClient) {
        // Retrieves the latest location and displays it

        Log.v(TAG,client.toString())
        client.lastLocation
            .addOnSuccessListener { location : Location? ->
                Log.v(TAG, "Success")
                if (location !== null){
                    val loc = location.latitude.toString()
                    Snackbar.make(view, loc, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                }else{
                    Log.v(TAG, "Location is $location")
                }
        }

    }

    fun createLocationRequest() {
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
            Log.v(TAG, "All permissions OK: $locationSettingsResponse")}

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
    }
}
