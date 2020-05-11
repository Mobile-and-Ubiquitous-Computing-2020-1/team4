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

            val POI = intent?.getStringExtra("POI")?.let { MainActivity.fromJson<POI>(it) }
            if (POI != null) {
                Log.i(TAG, POI.name)
            }

            // Query wikipedia
            val wikiManager = WikiInfoManager()
            //val placeInfo = wikiManager.getPlaceInfo(POI!!)
            // Log the information
            // Log.i(TAG, placeInfo.toString())

            // TODO create a geofenceTransitionsDetails serializable object to pass to the GeofenceTriggeredActivity
            val geofenceTransitionDetails =
                "You crossed the Geofence with ID:${triggerinGeofence.requestId} - Cool dude ${POI?.name}"
            // Send notification and log the transition details
            if (context != null) {
                sendNotification(
                    context,
                    "Cicerone geofence",
                    geofenceTransitionDetails,
                    1337,
                    geofenceTransitionDetails
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
        transitionDetails: String
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