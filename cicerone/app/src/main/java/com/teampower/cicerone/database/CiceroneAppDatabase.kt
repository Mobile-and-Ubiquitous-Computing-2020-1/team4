package com.teampower.cicerone.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.teampower.cicerone.database.history_table.POISavedDao
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
                    // Add sample POI.
                    val poiH = POIHistoryData(
                        "1337XD",
                        "SNU",
                        "School",
                        "January 1, 1970, 00:00:00 GMT",
                        0.00,
                        0.00
                    )
                    poiHDao.insert(poiH)

                    //************* POI Saved table *************
                    val poiSDao = database.poiSavedDao()

                    //************* Category table *************
                    val catDao = database.categoryDao()
                    // Delete all content here.
                    catDao.deleteAll() // Only when resetting
                    // Initialize table
                    val cat = CategoryData("School", 1.0)
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