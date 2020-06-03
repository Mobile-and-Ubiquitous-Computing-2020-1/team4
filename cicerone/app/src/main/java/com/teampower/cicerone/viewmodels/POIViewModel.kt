package com.teampower.cicerone.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.teampower.cicerone.MainActivity
import com.teampower.cicerone.POI
import com.teampower.cicerone.R
import com.teampower.cicerone.database.CiceroneAppDatabase
import com.teampower.cicerone.database.POIData
import com.teampower.cicerone.database.POIHistoryData
import com.teampower.cicerone.database.POISavedData
import com.teampower.cicerone.database.repositories.POIRepository
import com.teampower.cicerone.wikipedia.WikipediaPlaceInfo
import kotlinx.coroutines.*
import org.threeten.bp.ZonedDateTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet
import kotlin.coroutines.CoroutineContext

const val TAG = "POIViewModel"

/*
* https://codelabs.developers.google.com/codelabs/android-room-with-a-view-kotlin/#8
* Your activities and fragments are responsible for drawing data to the screen,
* while your ViewModel can take care of holding and processing all the data needed for the UI.
* */

abstract class POIViewModel<T>(application: Application) : AndroidViewModel(application),
    CoroutineScope {
    // Coroutine's background job
    private val job = Job()

    // Define default thread for Coroutine as Main and add job
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job
    abstract val repository: POIRepository<T>

    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    abstract val recentSavedPOIs: LiveData<List<T>>
    abstract val allPOI: LiveData<List<T>>

    /**
     * Launching a new coroutine to insert the data in a non-blocking way. We don't want insert
     * to block the main thread, so we're launching a new coroutine and calling the repository's
     * insert, which is a suspend function.
     */
    fun favorite(poi: T) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(poi)
    }

    fun unFavorite(foursquareId: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.removePOI(foursquareId)
    }

    suspend fun loadPOI(foursquareId: String) = viewModelScope.async(Dispatchers.IO) {
        repository.loadPOI(foursquareId)
    }

    fun toggleFavorite(poi: POI, poiData: T, context: Context, starWrapper: Any) {
        launch(Dispatchers.IO) {
            val result = loadPOI(poi.id).await()
            val isSaved = result !== null

            if (!isSaved) {
                // Add to favorites
                favorite(poiData)
                when (starWrapper) {
                    is FloatingActionButton -> {
                        DrawableCompat.setTint(
                            DrawableCompat.wrap(starWrapper.drawable),
                            ContextCompat.getColor(context, R.color.yellow)
                        )
                    }
                    is ImageView -> {
                        DrawableCompat.setTint(
                            DrawableCompat.wrap(starWrapper.drawable),
                            ContextCompat.getColor(context, R.color.yellow)
                        )
                    }
                    else -> throw IllegalArgumentException("Element does not have a drawable")
                }

                Log.i(TAG, "Added POI to favorites")

            } else {
                // Remove from favorites
                unFavorite(poi.id)
                when (starWrapper) {
                    is FloatingActionButton -> {
                        DrawableCompat.setTint(
                            DrawableCompat.wrap(starWrapper.drawable),
                            ContextCompat.getColor(context, android.R.color.darker_gray)
                        )
                    }
                    is ImageView -> {
                        DrawableCompat.setTint(
                            DrawableCompat.wrap(starWrapper.drawable),
                            ContextCompat.getColor(context, android.R.color.darker_gray)
                        )
                    }
                    else -> throw IllegalArgumentException("Element does not have a drawable")
                }
                Log.i(TAG, "Removed POI from favorites")
            }

        }
    }

    fun insert(poiData: T) {
        launch(Dispatchers.IO) {
            // Add to history
            repository.insert(poiData)
            Log.i(TAG, "Added POI to history")


        }
    }

    fun convertPOIDataToPOI(data: POIData): POI {
        return POI(
            id = data.foursquareID,
            name = data.name,
            category = data.category,
            categoryID = data.categoryID,
            lat = data.latitude,
            long = data.longitude,
            description = data.description,
            distance = data.distance,
            address = data.address,
            wikipediaInfo = data.wikipediaInfoJSON?.let {
                MainActivity.fromJson<WikipediaPlaceInfo>(
                    it
                )
            })
    }
}


class POISavedViewModel(application: Application) : POIViewModel<POISavedData>(application),
    CoroutineScope {
    override val repository: POIRepository<POISavedData>

    override val recentSavedPOIs: LiveData<List<POISavedData>>
    override val allPOI: LiveData<List<POISavedData>>

    init {
        val poisDao = CiceroneAppDatabase.getDatabase(
            application,
            viewModelScope
        ).poiSavedDao()
        repository = POIRepository(poisDao)
        recentSavedPOIs = repository.recentSavedPOIs
        allPOI = repository.allPOI
    }

    fun toggleFavorite(poi: POI, context: Context, starWrapper: Any) {
        val currentTimeString = ZonedDateTime.now().toString()

        val poiData = POISavedData(
            foursquareID = poi.id,
            name = poi.name,
            category = poi.category,
            categoryID = poi.categoryID,
            timeTriggered = currentTimeString,
            latitude = poi.lat,
            longitude = poi.long,
            description = poi.description,
            distance = poi.distance,
            address = poi.address,
            wikipediaInfoJSON = MainActivity.toJson(poi.wikipediaInfo)
        )
        super.toggleFavorite(poi, poiData, context, starWrapper)
    }
}


class POIHistoryViewModel(application: Application) : POIViewModel<POIHistoryData>(application),
    CoroutineScope {
    override val repository: POIRepository<POIHistoryData>
    override val recentSavedPOIs: LiveData<List<POIHistoryData>>
    override val allPOI: LiveData<List<POIHistoryData>>

    init {
        val poisDao = CiceroneAppDatabase.getDatabase(
            application,
            viewModelScope
        ).poiHistoryDao()
        repository = POIRepository(poisDao)
        recentSavedPOIs = repository.recentSavedPOIs
        allPOI = repository.allPOI
    }

    fun insert(poi: POI) {
        val currentTimeString = ZonedDateTime.now().toString()

        val poiData = POIHistoryData(
            foursquareID = poi.id,
            name = poi.name,
            category = poi.category,
            categoryID = poi.categoryID,
            timeTriggered = currentTimeString,
            latitude = poi.lat,
            longitude = poi.long,
            description = poi.description,
            distance = poi.distance,
            address = poi.address,
            wikipediaInfoJSON = MainActivity.toJson(poi.wikipediaInfo)
        )
        super.insert(poiData)
    }
}