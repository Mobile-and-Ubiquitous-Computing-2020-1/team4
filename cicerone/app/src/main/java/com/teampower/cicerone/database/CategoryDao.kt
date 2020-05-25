package com.teampower.cicerone.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.teampower.cicerone.database.CategoryData

/*
    Interface for table containing user specific weighting of the different categories.
    Used for recommendations algorithm.
*/

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category_table")
    fun getAll(): LiveData<List<CategoryData>>

    @Query("UPDATE category_table SET likes = likes + 1 WHERE foursquareID = (:foursquareID)")
    fun like(foursquareID: String): Int

    @Query("UPDATE category_table SET dislikes = dislikes + 1 WHERE foursquareID = (:foursquareID)")
    fun dislike(foursquareID: String): Int

    @Query("DELETE FROM category_table")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cat: CategoryData)

    @Delete
    fun deleteCategory(category: CategoryData)
}