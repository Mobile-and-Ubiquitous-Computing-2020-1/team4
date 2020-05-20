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

    @Query("UPDATE category_table SET score = score + (:points) WHERE name = (:catName)")
    fun updateCategoryPoints(catName: String, points: Double): Int

    @Query("SELECT * FROM category_table WHERE name = (:catName)")
    fun getCategoryPoints(catName: String): Double

    @Query("DELETE FROM category_table")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cat: CategoryData)

    @Delete
    fun deleteCategory(category: CategoryData)
}