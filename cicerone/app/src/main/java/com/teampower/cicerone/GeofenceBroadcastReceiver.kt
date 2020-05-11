package com.teampower.cicerone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent


class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "Geofencer"

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        Log.i(TAG, "Received geofence")
        if (geofencingEvent.hasError()) {
            val errorMessage = "Error when receiving geofencing event"
            Log.e(TAG, errorMessage)
        }

        // Get the transition type
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
        ) {

            // Get the geofences that were triggered. A single even can trigger
            // multiple geofences
            val triggerinGeofence = geofencingEvent.triggeringGeofences[0]

            // TODO: This is basically demo #2 for mid-term - only need to get Wikipedia info as string
            // Extract the transitionDetails

            var POI = intent?.getStringExtra("POI")?.let { MainActivity.fromJson<POI>(it) }
            if (POI != null) {
                Log.i(TAG, POI.name)
                // Query wikipedia
                val wikiManager = WikiInfoManager()
                val placeInfo = wikiManager.getPlaceInfo("Nakseongdae")
                // POI.description = placeInfo.toString()
            }

            // TODO create a geofenceTransitionsDetails serializable object to pass to the GeofenceTriggeredActivity
            val geofenceTransitionDetails = MainActivity.toJson(POI)
            // Send notification and log the transition details
            if (context != null) {
                sendNotification(
                    context,
                    "Cicerone geofence",
                    "You reached ${POI!!.name}",
                    1337,
                    POI
                )
            }
            Log.i(TAG, geofenceTransitionDetails)
        } else {
            // Log the error
            Log.e(TAG, "Error in Geofencer - should log error here one of these days")
        }
    }

    private fun sendNotification(
        context: Context,
        title: String,
        content: String,
        notificationId: Int,
        transitionDetails: POI
    ) {
        val notCon = NotificationsController()
        notCon.sendNotificationTriggeredGeofence(
            context,
            title,
            content,
            notificationId,
            transitionDetails
        )
    }
}