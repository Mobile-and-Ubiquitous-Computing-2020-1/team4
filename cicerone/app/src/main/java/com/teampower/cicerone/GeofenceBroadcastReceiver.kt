package com.teampower.cicerone

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
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
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL){

            // Get the geofences that were triggered. A single even can trigger
            // multiple geofences
            val triggerinGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String
            val geofenceTransitionDetails = "You crossed the Geofence - Cool dude"
            // Send notification and log the transition details
            if (context != null) {
                sendNotification(context, "Cicerone geofence", geofenceTransitionDetails, 1337)
            }
            Log.i(TAG, geofenceTransitionDetails)
        } else {
            // Log the error
            Log.e(TAG, "Error in Geofencer - should log error here one of these days")
        }
    }

    private fun sendNotification(context: Context, title: String, content: String, notificationId: Int) {
        val notCon = NotificationsController()
        notCon.sendNotification(context, title, content, notificationId)
    }
}