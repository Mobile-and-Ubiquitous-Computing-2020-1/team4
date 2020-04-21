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


    fun getClient(context: Context): GeofencingClient? {
        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back
            // when calling addGeofences() and removeGeofences().
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val geofencingClient = LocationServices.getGeofencingClient(context)
        geofencingClient?.addGeofences(getGeofencingRequest(), geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.v(TAG_GEO, "Geofences added")
            }
            addOnFailureListener{
                Log.v(TAG_GEO, "Failed to add geofences")
            }
        }

        return geofencingClient
    }

    private fun getGeofenceList(): List<Geofence> {
        var geofence = Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence
            .setRequestId("Fence1")
            // Set thh circular region of this geofence. Distance in meters.
            .setCircularRegion(
                37.4608,
                -122.1384,
                200F
            )
            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            // Create the geofence
            .build()

        return listOf<Geofence>(geofence)
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply{
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(getGeofenceList())
        }.build()
    }
}
