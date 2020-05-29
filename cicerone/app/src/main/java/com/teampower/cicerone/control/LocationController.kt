package com.teampower.cicerone.control

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.teampower.cicerone.MY_PERMISSIONS_REQUEST_LOCATION_ID
import com.teampower.cicerone.MainActivity
import com.teampower.cicerone.TAG

class LocationController() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var lastLocation: android.location.Location
    private var requestingLocationUpdates = true

    fun startLocation(context: Context, activity: MainActivity, dataCon: DataController) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = createLocationRequest(context, activity)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                lastLocation = locationResult.lastLocation
            }
        }
    }

    fun getLocation(): android.location.Location {
        while (!::lastLocation.isInitialized) {
            // block and wait
        }
        Log.d(TAG, "returned")
        return lastLocation
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun enableLocationTracking(context: Context, activity: MainActivity) {
        // developer.android.com/training/location/request-updates#request-background-location
        val permissionAccessFineLocationApproved = ActivityCompat
            .checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        val permissionBackgroundLocationApproved = ActivityCompat
            .checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        if (!permissionAccessFineLocationApproved) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user why we need this permission.
                // After the user sees the explanation, try to request the permission again.
                showLocationPermissionRationaleDialog(context, activity)
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    activity,
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
                    activity,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            ) {
                // Show an explanation to the user why we need this permission.
                // After the user sees the explanation, try to request the permission again.
                showLocationPermissionRationaleDialog(context, activity)
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    activity,
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

    fun onRequestPermissionsResult(
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

    private fun showLocationPermissionRationaleDialog(context: Context, activity: MainActivity) {
        val alert = AlertDialog.Builder(context)

        alert.setTitle("Required Location Permission")
        alert.setMessage("We need to access your location so we can send you recommendations on the go.")
        alert.setPositiveButton("OK") { dialog, which ->
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION_ID
            )
        }
        alert.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        alert.create().show()
    }

    private fun requestLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        Log.v(TAG, "Location updates started")
    }

    private fun createLocationRequest(context: Context, activity: MainActivity): LocationRequest {
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
        val client: SettingsClient = LocationServices.getSettingsClient(context)
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
                    exception.startResolutionForResult(activity, 1)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore error
                }
            }
        }
        return locationRequest
    }
}