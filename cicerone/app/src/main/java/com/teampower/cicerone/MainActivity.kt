package com.teampower.cicerone

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_scrolling.*

const val MY_PERMISSIONS_REQUEST_LOCATION_ID = 99
const val CHANNEL_ID = "CiceroneComms1337"
const val TAG = "Cicerone"
const val TAG_GEO = "Geofencer"

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val notCon = NotificationsController()
    private val geoCon = GeofencingController()
    private var requestingLocationUpdates = true
    @RequiresApi(Build.VERSION_CODES.Q)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)

        user_location.text = getString(R.string.user_position, "-", "-")

        // Setup location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = createLocationRequest()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    Log.v(TAG, location.toString())
                    user_location.text = getString(
                        R.string.user_position,
                        location.latitude.toString(),
                        location.longitude.toString()
                    )
                }
            }
        }
        // Setup geofencing services
        geofencingClient = geoCon.getClient(this)!!

        // Setup notifications
        notCon.createNotificationChannel(this)

        // Setup fab to test notifications
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Notification sent", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            notCon.sendNotification(this, "Test notification", "Hi, I'm the notification that was sent", 1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        Log.v(TAG, "Activity resumed")
        enableLocationTracking()
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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun enableLocationTracking() {
        // developer.android.com/training/location/request-updates#request-background-location
        val permissionAccessFineLocationApproved = ActivityCompat
            .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        val permissionBackgroundLocationApproved = ActivityCompat
            .checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        if (!permissionAccessFineLocationApproved) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user why we need this permission.
                // After the user sees the explanation, try to request the permission again.
                showLocationPermissionRationaleDialog()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION_ID
                )
            }
        } else {
            // Permission has already been granted
            Log.d(TAG, "Location permission already granted")
            requestLocationUpdates()
        }

        if (!permissionBackgroundLocationApproved) {
            // TODO Duplicating for now, this will have to be refactored
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            ) {
                // Show an explanation to the user why we need this permission.
                // After the user sees the explanation, try to request the permission again.
                showLocationPermissionRationaleDialog()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION_ID
                )
            }
        } else {
            // Permission has already been granted
            Log.d(TAG, "Background location permission already granted")
            requestLocationUpdates()
        }
    }

    private fun showLocationPermissionRationaleDialog() {
        val alert = AlertDialog.Builder(this)

        alert.setTitle("Required Location Permission")
        alert.setMessage("We need to access your location so we can send you recommendations on the go.")
        alert.setPositiveButton("OK") { dialog, which ->
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION_ID
            )
        }
        alert.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        alert.create().show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION_ID -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the task you need to do.
                    Log.d(TAG, "Location permission granted")
                    requestLocationUpdates()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d(TAG, "Location permission denied by user")

                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun requestLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        Log.v(TAG, "Location updates started")
    }

    private fun createLocationRequest(): LocationRequest {
        // https://developer.android.com/training/location/change-location-settings
        Log.v(TAG, "Setting up location services")
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        // Get current location settings and device capability
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locSettingsResp ->
            requestingLocationUpdates = locSettingsResp.locationSettingsStates.isGpsPresent &&
                    locSettingsResp.locationSettingsStates.isGpsUsable &&
                    locSettingsResp.locationSettingsStates.isLocationPresent &&
                    locSettingsResp.locationSettingsStates.isLocationUsable
            Log.v(
                TAG,
                "Location settings checked. Able to retrieve location: $requestingLocationUpdates"
            )
        }

        task.addOnFailureListener { exception ->
            Log.v(TAG, "Not able to retrieve location due to device settings or capability.")
            if (exception is ResolvableApiException) {
                try {
                    // Show dialog for user to allow location settings
                    exception.startResolutionForResult(this@MainActivity, 1)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore error
                }
            }
        }
        return locationRequest
    }


}
