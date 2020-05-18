package com.teampower.cicerone.database.category_table

import androidx.lifecycle.LiveData
import com.teampower.cicerone.database.CategoryData

/*
* CategoryRepository
* Repository for category_table
* A Repository manages queries and allows you to use multiple backends.
* E.g. Implements the logic for deciding whether to fetch data from a network or use results cached in a local database.
* */

// No need to expose the entire database to the repository, so only send in DAO.
class CategoryRepository(private val catDao: CategoryDao) {
    // Room executes all queries on a separate thread.

    val allCatScores: LiveData<List<CategoryData>> = catDao.getAll()

    suspend fun insert(cat: CategoryData) {
        catDao.insert(cat)
    }
    fun updateCategoryPoints(catName: String, points: Double) {
        catDao.updateCategoryPoints(catName, points)
    }
    fun getCategoryPoints(catName: String) {
        catDao.getCategoryPoints(catName)
    }
}