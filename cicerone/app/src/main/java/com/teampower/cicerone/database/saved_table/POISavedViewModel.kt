package com.teampower.cicerone.database.history_table

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.teampower.cicerone.database.CiceroneAppDatabase
import com.teampower.cicerone.database.POISavedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
* https://codelabs.developers.google.com/codelabs/android-room-with-a-view-kotlin/#8
* Your activities and fragments are responsible for drawing data to the screen,
* while your ViewModel can take care of holding and processing all the data needed for the UI.
* */

class POISavedViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository: POISavedRepository

    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allPOI: LiveData<List<POISavedData>>

    init {
        val poisDao = CiceroneAppDatabase.getDatabase(
            application,
            viewModelScope
        ).poiSavedDao()
        historyRepository =
            POISavedRepository(poisDao)
        allPOI = historyRepository.allPOI
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way. We don't want insert
     * to block the main thread, so we're launching a new coroutine and calling the repository's
     * insert, which is a suspend function.
     */
    fun insert(poi: POISavedData) = viewModelScope.launch(Dispatchers.IO) {
        historyRepository.insert(poi)
    }
}