package com.teampower.cicerone.database.history_table

import androidx.lifecycle.LiveData
import androidx.room.*
import com.teampower.cicerone.database.POISavedData

/*
* POIDao
* Contains the methods used for accessing the database.
* */

// Saved poi table interface
@Dao
interface POISavedDao {
    @Query("SELECT * FROM poi_saved_table")
    fun getAll(): LiveData<List<POISavedData>>

    @Query("SELECT * from poi_saved_table ORDER BY time_triggered ASC")
    fun getRecentlyTriggered(): LiveData<List<POISavedData>>

    @Query("SELECT * from poi_saved_table where foursquareID = (:foursquareID)")
    fun loadPOI(foursquareID: String): LiveData<POISavedData?>?

    @Delete
    fun deletePOI(poi: POISavedData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(poi: POISavedData)

    @Query("DELETE FROM poi_saved_table")
    suspend fun deleteAll()
}