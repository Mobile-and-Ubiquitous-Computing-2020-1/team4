package com.teampower.cicerone.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [POIData::class, CategoryData::class], version = 1)
public abstract class CiceroneAppDatabase : RoomDatabase() {

    // abstract fun POIDao(): POIDao
    abstract fun poiDao(): POIDao
    abstract fun categoryDao(): CategoryDao

    private class CiceroneDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    //************* POI table *************
                    val poiDao = database.poiDao()
                    // Delete all content here.
                    poiDao.deleteAll()
                    // Add sample POI.
                    var poi = POIData("1337XD", "SNU", "School", "12:00", 0.00, 0.00)
                    poiDao.insert(poi)

                    //************* Category table *************
                    val catDao = database.categoryDao()
                    // Delete all content here.
                    catDao.deleteAll()
                    // Add sample categories with score.
                    var cat = CategoryData("School", 10.0)
                    catDao.insert(cat)
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