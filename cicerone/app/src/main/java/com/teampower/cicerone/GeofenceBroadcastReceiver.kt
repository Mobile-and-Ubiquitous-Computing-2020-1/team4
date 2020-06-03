package com.teampower.cicerone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.teampower.cicerone.control.NotificationsController
import com.teampower.cicerone.database.CiceroneAppDatabase
import com.teampower.cicerone.database.POIHistoryData
import com.teampower.cicerone.database.repositories.POIRepository
import com.teampower.cicerone.wikipedia.RestAPI
import com.teampower.cicerone.wikipedia.WikipediaPlaceInfo
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class GeofenceBroadcastReceiver() : BroadcastReceiver() {
    private val TAG = "Geofencer"
    private val api: RestAPI =
        RestAPI()
    private lateinit var poiSerialized: String
    private lateinit var poiObject: POI
    //private lateinit var poiHistoryViewModel: POIHistoryViewModel
    //private lateinit var poiHistory: Set<String>

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        Log.i(TAG, "Received geofence: $geofencingEvent")
        if (geofencingEvent.hasError()) {
            val errorMessage = "Error when receiving geofencing event"
            Log.e(TAG, errorMessage)
        }

        // Get the transition type
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Get the geofences that were triggered. A single even can trigger
            // multiple geofences
            val triggeringGeofence = geofencingEvent.triggeringGeofences[0]
            // Remove the geofence - no need to remove the geofence, it won't trigger again.
            // The second trigger we are experiencing is because it is added to the geofences again from Foursquare.
            // Extract the transitionDetails
            poiSerialized = intent?.getStringExtra("POI")
                ?: "" // TODO: Handle error case better. This works only assuming we always get a serialized POI object
            poiObject = MainActivity.fromJson(poiSerialized)
            Log.i(TAG, "id" + triggeringGeofence.requestId + " got the poi name " + poiObject.name)
            GlobalScope.launch {
                val poisDao = CiceroneAppDatabase.getDatabase(context!!, this).poiHistoryDao()
                val repository = POIRepository(poisDao)
                val ids = async(Dispatchers.IO) {
                    val historyData = repository.getAll()
                    val ids = historyData.map { it.foursquareID }.toHashSet()
                    return@async ids
                }
                if (!ids.await().contains(triggeringGeofence.requestId)) {
                    getPlaceInfoSendNotification(context)
                } else {
                    Log.d(TAG, "${poiObject.name} is already in history, we do not need to send a new notification.")
                }
            }
        } else {
            // Log the error
            Log.e(TAG, "Error in geofencer - geofence transition not interesting")
        }
    }

    private fun sendNotification(
        context: Context,
        poiObject: POI
    ) {
        val contentText =
            "You are close to ${poiObject.name}. Want to go have a look? It's ${poiObject.distance}m away."
        val notCon = NotificationsController()
        notCon.sendNotificationTriggeredGeofence(
            context,
            "New recommendation nearby!",
            contentText,
            1337,
            MainActivity.toJson(poiObject)
        )
    }

    private fun getPlaceInfoSendNotification(context: Context?): WikipediaPlaceInfo? {
        var wikiPlaceInfo: WikipediaPlaceInfo? = null
        // Query wikipedia
        val filterName = poiObject.name.split(" (")[0]
        api.getPlaceInfo(filterName)
            .enqueue(object : Callback<WikipediaPlaceInfo> {
                override fun onResponse(
                    call: Call<WikipediaPlaceInfo>,
                    response: Response<WikipediaPlaceInfo>
                ) {
                    wikiPlaceInfo = response.body()

                    println("wikiPlaceInfo for ${poiObject.name}: $wikiPlaceInfo")
                    // Log the information
                    Log.i(TAG, wikiPlaceInfo.toString())

                    // Combine info into one object
                    poiObject.wikipediaInfo = wikiPlaceInfo

                    // Send notification and log the transition details
                    if (context != null) {
                        sendNotification(
                            context,
                            poiObject
                        )
                    }
                    Log.i(TAG, "" + poiObject.wikipediaInfo?.extract)
                }

                override fun onFailure(call: Call<WikipediaPlaceInfo>, t: Throwable) =
                    t.printStackTrace()
            })
        return wikiPlaceInfo
    }
}