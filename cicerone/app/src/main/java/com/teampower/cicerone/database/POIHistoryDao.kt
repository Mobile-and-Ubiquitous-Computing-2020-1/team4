package com.teampower.cicerone.database

import androidx.lifecycle.LiveData
import androidx.room.*

/*
* POIDao
* Contains the methods used for accessing the database.
* */

// History/saved places table interface
@Dao
interface POIHistoryDao {
    @Query("SELECT * FROM poi_history_table")
    fun getAll(): LiveData<List<POIHistoryData>>

    @Query("SELECT * from poi_history_table ORDER BY time_triggered ASC")
    fun getRecentlyTriggered(): LiveData<List<POIHistoryData>>

//    @Query("SELECT 1 from poi_table WHERE apiId =:foursquareID")
//    fun getById(id: String): POIData

    @Query("SELECT * from poi_history_table where foursquareID = (:foursquareID)")
    fun loadPOI(foursquareID: String): LiveData<POIHistoryData?>?

    @Delete
    fun deletePOI(poi: POIHistoryData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(poi: POIHistoryData)

    @Query("DELETE FROM poi_history_table")
    suspend fun deleteAll()
}


/*
@Dao
interface WordDao {
    @Query("SELECT * from word_table ORDER BY word ASC")
    fun getAlphabetizedWords(): LiveData<List<Word>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: Word)

    @Query("DELETE FROM word_table")
    suspend fun deleteAll()
}
*/
/* Examples
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query(
        "SELECT * FROM user WHERE first_name LIKE :first AND " +
                "last_name LIKE :last LIMIT 1"
    )
    fun findByName(first: String, last: String): User

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}*/
