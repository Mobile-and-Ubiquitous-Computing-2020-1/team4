package com.teampower.cicerone.database

import androidx.lifecycle.LiveData

/*
* POIRepository
* A Repository manages queries and allows you to use multiple backends.
* E.g. Implements the logic for deciding whether to fetch data from a network or use results cached in a local database.
* */

// No need to expose the entire database to the repository, so only send in DAO.
class POIRepository(private val poiDao: POIDao) {
    // Room executes all queries on a separate thread.

    // Tutorial test stuff
    // Observed LiveData will notify the observer when the data has changed.
    val allPOI: LiveData<List<POIData>> = poiDao.getRecentlyTriggered()

    suspend fun insert(poi: POIData) {
        poiDao.insert(poi)
    }
}