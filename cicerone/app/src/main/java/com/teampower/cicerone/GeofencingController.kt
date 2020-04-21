package com.teampower.cicerone

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofencingController() {
    private lateinit var geofencingClient: GeofencingClient
    private var geofenceList = ArrayList<Geofence>()

    fun startGeofencing(context: Context) {
        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back
            // when calling addGeofences() and removeGeofences().
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        geofencingClient = LocationServices.getGeofencingClient(context)
        initializeGeofenceList()
        geofencingClient?.addGeofences(getGeofencingRequest(), geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.v(TAG_GEO, "Geofences added")
            }
            addOnFailureListener{
                Log.v(TAG_GEO, "Failed to add geofences")
            }
        }
    }

    private fun initializeGeofenceList() {
        val geofence = createGeofence(37.4608, -122.1384, 200F, Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
        addGeofence(geofence)
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply{
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    fun createGeofence(lat: Double, long: Double, radius: Float, expiration: Long, transTypes: Int) : Geofence{
        val geofence = Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence
            .setRequestId("Fence1")
            // Set thh circular region of this geofence. Distance in meters.
            .setCircularRegion(
                lat,
                long,
                radius
            )
            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time
            .setExpirationDuration(expiration)
            .setTransitionTypes(transTypes)
            // Create the geofence
            .build()
        return geofence
    }

    fun addGeofence(geofence: Geofence){
        geofenceList.plusAssign(geofence)
    }
}
