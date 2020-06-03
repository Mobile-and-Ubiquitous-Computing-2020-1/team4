package com.teampower.cicerone.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [POIHistoryData::class, POISavedData::class, CategoryData::class], version = 1)
public abstract class CiceroneAppDatabase : RoomDatabase() {

    // abstract fun POIDao(): POIDao
    abstract fun poiHistoryDao(): POIHistoryDao
    abstract fun poiSavedDao(): POISavedDao
    abstract fun categoryDao(): CategoryDao

    private class CiceroneDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    //************* POI History table *************
                    val poiHDao = database.poiHistoryDao()
                    // Delete all content here.
                    poiHDao.deleteAll()
                    /*
                    // Add sample POI.
                    val poiH = POIHistoryData(
                        foursquareID = "1337XD",
                        name = "The Most Wonderful University",
                        category = "School",
                        categoryID = "1",
                        timeTriggered = "2020-05-28T18:16:41.905Z",
                        latitude = 0.00,
                        longitude = 0.00,
                        description = "Founded in 1912, this is the oldest and most famous university in today's world.",
                        distance = 20,
                        address = "Galaxyroad 18, 290192x PoIS, Sunnyland"
                    )
                    poiHDao.insert(poiH)*/

                    //************* Category table *************
                    val catDao = database.categoryDao()
                    // Delete all content here.
                    catDao.deleteAll() // Only when resetting
                    // Initialize table
                    val cat1 = CategoryData("52e81612bcbc57f1066b7a14", "Palace", 1, 1000000000)
                    catDao.insert(cat1)
                    //val cat2 = CategoryData("50aaa49e4b90af0d42d5de11", "Castle", 1, 1)
                    //catDao.insert(cat2)
                }
            }
        }
    }

    companion object {
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: CiceroneAppDatabase? = null

        fun getDatabase(
            context: Context, scope: CoroutineScope
        ): CiceroneAppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CiceroneAppDatabase::class.java,
                    "cicerone_app_database"
                )
                    .addCallback(CiceroneDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}