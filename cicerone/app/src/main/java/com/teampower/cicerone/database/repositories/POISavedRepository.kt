package com.teampower.cicerone.database.history_table

import androidx.lifecycle.LiveData
import com.teampower.cicerone.database.POISavedData

/*
* POIRepository
* A Repository manages queries and allows you to use multiple backends.
* E.g. Implements the logic for deciding whether to fetch data from a network or use results cached in a local database.
* */

// TODO: Remove one of the Repositories. Data structure is same for history and saved,
//  so can just use the same Repository and send in different dao based on which table you want.

// No need to expose the entire database to the repository, so only send in DAO.
class POISavedRepository(private val savedPoiDao: POISavedDao) {
    // Room executes all queries on a separate thread.

    // Tutorial test stuff
    // Observed LiveData will notify the observer when the data has changed.
    val recentSavedPOIs: LiveData<List<POISavedData>> = savedPoiDao.getRecentlyTriggered()
    val allPOI: LiveData<List<POISavedData>> = savedPoiDao.getAllByAscTimeTriggered()

    suspend fun insert(poi: POISavedData) {
        savedPoiDao.insert(poi)
    }

    suspend fun loadPOI(foursquareID: String): POISavedData? {
        return savedPoiDao.loadPOI(foursquareID)
    }

    suspend fun removePOI(foursquareID: String) {
        savedPoiDao.deletePOIbyId(foursquareID)
    }
}