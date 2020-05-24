package com.teampower.cicerone.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poi_history_table")
data class POIHistoryData(
    @PrimaryKey val foursquareID: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "time_triggered") val timeTriggered: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double
)

@Entity(tableName = "poi_saved_table")
data class POISavedData(
    @PrimaryKey val foursquareID: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "time_triggered") val timeTriggered: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double
)

@Entity(tableName = "category_table")
data class CategoryData(
    @PrimaryKey val name: String,
    @ColumnInfo val score: Double
)