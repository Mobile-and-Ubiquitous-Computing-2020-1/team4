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


    fun startGeofencing(context: Context) {
        geofencingClient = LocationServices.getGeofencingClient(context)
    }

    fun addGeofence(geofence: Geofence, context: Context, poi: POI) {
        val geofenceAddPendingIntent: PendingIntent by lazy {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)

            intent.putExtra("POI", MainActivity.toJson(poi))
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back
            // when calling addGeofences() and removeGeofences().
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        geofencingClient.addGeofences(getGeofencingRequest(geofence), geofenceAddPendingIntent)
            ?.run {
                addOnSuccessListener {
                    Log.v(
                        TAG_GEO,
                        "Geofence with ID:${geofence.requestId} and name:${poi.name} and lat:${poi.lat} and long:${poi.long} and distance:${poi.distance} added"
                    )
                }
                addOnFailureListener { exception ->
                    run {
                        Log.v(
                            TAG_GEO,
                            "Failed to add geofence with ID:${geofence.requestId} - Error: $exception"
                        )
                        Log.v(TAG_GEO, "GeofencingClient:$geofencingClient.")
                    }

                }
            }
    }

    fun removeGeofence(geofenceID: String, context: Context) {
        geofencingClient?.removeGeofences(listOf(geofenceID))?.run {
            addOnSuccessListener {
                Log.v(TAG_GEO, "Geofence with ID:$geofenceID removed")
            }
            addOnFailureListener {
                Log.v(TAG_GEO, "Failed to remove geofence with ID:$geofenceID")
            }
        }
    }

    fun createGeofence(
        lat: Double,
        long: Double,
        id: String,
        radius: Float,
        expiration: Long,
        transitionTypes: Int
    ): Geofence {
        val geofence = Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence
            .setRequestId(id)
            // Set the circular region of this geofence. Distance in meters.
            .setCircularRegion(
                lat,
                long,
                radius
            )
            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time
            .setExpirationDuration(expiration)
            .setTransitionTypes(transitionTypes)
            // Create the geofence
            .build()
        return geofence
    }

    private fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }
}
