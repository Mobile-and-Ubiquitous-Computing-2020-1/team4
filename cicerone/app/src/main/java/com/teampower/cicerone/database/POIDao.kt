package com.teampower.cicerone.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/*
* POIDao
* Contains the methods used for accessing the database.
* */

/* // TODO: Replace tutorial Word stuff with this.
@Dao
interface POIDao {
    @Query("SELECT * FROM poi_table")
    fun getAll(): LiveData<List<POIData>>

    fun getRecentlyTriggered(): LiveData<List<POIData>>
    fun getById(id: String): POIData

    @Query("SELECT * from poi_table where apiId = :foursquareId")
    fun loadPOI(foursquareId: String): LiveData<POIData?>?

    fun deletePOI(foursquareId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(poi: POIData)

    @Query("DELETE FROM poi_table")
    suspend fun deleteAll()

}
*/
@Dao
interface WordDao {
    @Query("SELECT * from word_table ORDER BY word ASC")
    fun getAlphabetizedWords(): LiveData<List<Word>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: Word)

    @Query("DELETE FROM word_table")
    suspend fun deleteAll()
}

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
