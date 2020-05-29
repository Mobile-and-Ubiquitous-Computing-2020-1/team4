package com.teampower.cicerone.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

abstract class POIData {
    abstract val foursquareID: String
    abstract val name: String
    abstract val category: String
    abstract val timeTriggered: String
    abstract val latitude: Double
    abstract val longitude: Double
    abstract val description: String?
    abstract val distance: Int?
    abstract val address: String?
    abstract val wikipediaInfoJSON: String?

}

@Entity(tableName = "poi_history_table")
data class POIHistoryData(
    @PrimaryKey override val foursquareID: String,
    @ColumnInfo(name = "name") override val name: String,
    @ColumnInfo(name = "category") override val category: String,
    @ColumnInfo(name = "time_triggered") override val timeTriggered: String,
    @ColumnInfo(name = "latitude") override val latitude: Double,
    @ColumnInfo(name = "longitude") override val longitude: Double,
    @ColumnInfo(name = "description") override val description: String?,
    @ColumnInfo(name = "distance") override val distance: Int?,
    @ColumnInfo(name = "address") override val address: String?,
    @ColumnInfo(name = "wikipediaInfoJSON") override val wikipediaInfoJSON: String? = ""
) : POIData()

@Entity(tableName = "poi_saved_table")
data class POISavedData(
    @PrimaryKey override val foursquareID: String,
    @ColumnInfo(name = "name") override val name: String,
    @ColumnInfo(name = "category") override val category: String,
    @ColumnInfo(name = "time_triggered") override val timeTriggered: String,
    @ColumnInfo(name = "latitude") override val latitude: Double,
    @ColumnInfo(name = "longitude") override val longitude: Double,
    @ColumnInfo(name = "description") override val description: String?,
    @ColumnInfo(name = "distance") override val distance: Int?,
    @ColumnInfo(name = "address") override val address: String?,
    @ColumnInfo(name = "wikipediaInfoJSON") override val wikipediaInfoJSON: String? = ""
) : POIData()

@Entity(tableName = "category_table")
data class CategoryData(
    @PrimaryKey val name: String,
    @ColumnInfo val score: Double
)