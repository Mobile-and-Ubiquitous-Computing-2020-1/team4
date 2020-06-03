package com.teampower.cicerone.database

import androidx.lifecycle.LiveData
import androidx.room.*

/*
* POIDao
* Contains the methods used for accessing the database.
* */


interface POIDao<T> {
    suspend fun getAll(): List<@JvmSuppressWildcards T>
    fun getAllByAscTimeTriggered(): LiveData<List<T>>
    fun getAllByDescTimeTriggered(): LiveData<List<T>>
    fun getRecentlyTriggered(): LiveData<List<T>>
    suspend fun loadPOI(foursquareID: String): T?
    suspend fun deletePOI(poi: T)
    suspend fun deletePOIbyId(foursquareID: String)
    suspend fun insert(poi: T)
    suspend fun deleteAll()
}

// Saved poi table interface
@Dao
interface POISavedDao : POIDao<POISavedData> {
    @Query("SELECT * FROM poi_saved_table")
    override suspend fun getAll(): List<POISavedData>

    @Query("SELECT * FROM poi_saved_table ORDER BY time_triggered ASC")
    override fun getAllByAscTimeTriggered(): LiveData<List<POISavedData>>

    @Query("SELECT * FROM poi_saved_table ORDER BY time_triggered DESC")
    override fun getAllByDescTimeTriggered(): LiveData<List<POISavedData>>

    @Query("SELECT * from poi_saved_table ORDER BY time_triggered DESC limit 5")
    override fun getRecentlyTriggered(): LiveData<List<POISavedData>>

    @Query("SELECT * from poi_saved_table where foursquareID = (:foursquareID)")
    override suspend fun loadPOI(foursquareID: String): POISavedData?

    @Delete
    override suspend fun deletePOI(poi: POISavedData)

    @Query("DELETE FROM poi_saved_table where foursquareID=(:foursquareID)")
    override suspend fun deletePOIbyId(foursquareID: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(poi: POISavedData)

    @Query("DELETE FROM poi_saved_table")
    override suspend fun deleteAll()
}

// History/saved places table interface
@Dao
interface POIHistoryDao : POIDao<POIHistoryData> {
    @Query("SELECT * FROM poi_history_table")
    override suspend fun getAll(): List<POIHistoryData>

    @Query("SELECT * FROM poi_history_table ORDER BY time_triggered ASC")
    override fun getAllByAscTimeTriggered(): LiveData<List<POIHistoryData>>

    @Query("SELECT * FROM poi_history_table ORDER BY time_triggered DESC")
    override fun getAllByDescTimeTriggered(): LiveData<List<POIHistoryData>>

    @Query("SELECT * from poi_history_table ORDER BY time_triggered DESC limit 5")
    override fun getRecentlyTriggered(): LiveData<List<POIHistoryData>>

    @Query("SELECT * from poi_history_table where foursquareID = (:foursquareID)")
    override suspend fun loadPOI(foursquareID: String): POIHistoryData?

    @Delete
    override suspend fun deletePOI(poi: POIHistoryData)

    @Query("DELETE FROM poi_history_table where foursquareID=(:foursquareID)")
    override suspend fun deletePOIbyId(foursquareID: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(poi: POIHistoryData)

    @Query("DELETE FROM poi_history_table")
    override suspend fun deleteAll()
}