package com.teampower.cicerone.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.teampower.cicerone.database.CiceroneAppDatabase
import com.teampower.cicerone.database.POIHistoryData
import com.teampower.cicerone.database.POISavedData
import com.teampower.cicerone.database.repositories.POIRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/*
* https://codelabs.developers.google.com/codelabs/android-room-with-a-view-kotlin/#8
* Your activities and fragments are responsible for drawing data to the screen,
* while your ViewModel can take care of holding and processing all the data needed for the UI.
* */

abstract class POIViewModel<T>(application: Application) : AndroidViewModel(application) {

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
}


class POISavedViewModel(application: Application) : POIViewModel<POISavedData>(application) {
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
}


class POIHistoryViewModel(application: Application) : POIViewModel<POIHistoryData>(application) {
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
}