package com.teampower.cicerone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService


class NotificationsController() {

     fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    /**
     * Sends a notification to the user with the given content, which when tapped
     * moves the user to the MainActivity.
     */
    fun sendNotificationToMain(context: Context, title: String, content: String, notificationId: Int) {
        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.googleg_standard_color_18)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that fires when the user taps the notification
            .setContentIntent(pendingIntent)
            // Automatically close the notification when it has been tapped.
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification
            notify(notificationId, builder.build())
        }
    }

    /**
     * Sends a notification to the user with the given content, which when tapped
     * moves the user to GeofencedTriggeredActivity. This activity also has a back button
     * which leads to the MainActivity.
     */
    fun sendNotificationTriggeredGeofence(context: Context, title: String, content: String, notificationId: Int, transitionDetails: String) {
        // Create an Intent for the activity you want to start
        val geofenceIntent = Intent(context, GeofenceTriggeredActivity::class.java)
        geofenceIntent.putExtra("TRANSITION_DETAILS", transitionDetails)
        // Create the TaskStackBuilder
        val geofencePendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(geofenceIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.common_full_open_on_phone)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that fires when the user taps the notification
            .setContentIntent(geofencePendingIntent)
            // Automatically close the notification when it has been tapped.
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification
            notify(notificationId, builder.build())
        }
    }

}