package com.teampower.cicerone.database.repositories

import androidx.lifecycle.LiveData
import com.teampower.cicerone.database.POIDao

/*
* POIRepository
* A Repository manages queries and allows you to use multiple backends.
* E.g. Implements the logic for deciding whether to fetch data from a network or use results cached in a local database.
* */

// No need to expose the entire database to the repository, so only send in DAO.
class POIRepository<T>(private val poiDao: POIDao<T>) {
    // Room executes all queries on a separate thread.

    // Observed LiveData will notify the observer when the data has changed.
    val recentSavedPOIs: LiveData<List<T>> = poiDao.getRecentlyTriggered()
    val allPOI: LiveData<List<T>> = poiDao.getAllByAscTimeTriggered()

    suspend fun insert(poi: T) {
        poiDao.insert(poi)
    }

    suspend fun loadPOI(foursquareID: String): T? {
        return poiDao.loadPOI(foursquareID)
    }

    suspend fun getAll(): List<T> {
        return poiDao.getAll()
    }

    suspend fun removePOI(foursquareID: String) {
        poiDao.deletePOIbyId(foursquareID)
    }
}