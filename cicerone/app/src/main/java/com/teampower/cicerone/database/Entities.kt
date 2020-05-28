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
}

@Entity(tableName = "poi_history_table")
data class POIHistoryData(
    @PrimaryKey override val foursquareID: String,
    @ColumnInfo(name = "name") override val name: String,
    @ColumnInfo(name = "category") override val category: String,
    @ColumnInfo(name = "time_triggered") override val timeTriggered: String,
    @ColumnInfo(name = "latitude") override val latitude: Double,
    @ColumnInfo(name = "longitude") override val longitude: Double
) : POIData()

@Entity(tableName = "poi_saved_table")
data class POISavedData(
    @PrimaryKey override val foursquareID: String,
    @ColumnInfo(name = "name") override val name: String,
    @ColumnInfo(name = "category") override val category: String,
    @ColumnInfo(name = "time_triggered") override val timeTriggered: String,
    @ColumnInfo(name = "latitude") override val latitude: Double,
    @ColumnInfo(name = "longitude") override val longitude: Double
) : POIData()

@Entity(tableName = "category_table")
data class CategoryData(
    @PrimaryKey val name: String,
    @ColumnInfo val score: Double
)