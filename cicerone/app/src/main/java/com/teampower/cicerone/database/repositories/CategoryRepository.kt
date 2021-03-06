package com.teampower.cicerone.database.repositories

import androidx.lifecycle.LiveData
import com.teampower.cicerone.database.CategoryData
import com.teampower.cicerone.database.CategoryDao

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
    fun like(catName: String) {
        catDao.like(catName)
    }
    fun dislike(catName: String) {
        catDao.dislike(catName)
    }
}