package com.teampower.cicerone.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

interface POIData {
    val foursquareID: String
    val name: String
    val category: String
    val categoryID: String
    val timeTriggered: String
    val latitude: Double
    val longitude: Double
    val description: String?
    val distance: Int?
    val address: String?
    val wikipediaInfoJSON: String?
}

interface Ids {
    val foursquareID: String
}

@Entity(tableName = "poi_history_table")
data class POIHistoryData(
    @PrimaryKey override val foursquareID: String,
    @ColumnInfo(name = "name") override val name: String,
    @ColumnInfo(name = "category") override val category: String,
    @ColumnInfo(name = "category_id") override val categoryID: String,
    @ColumnInfo(name = "time_triggered") override val timeTriggered: String,
    @ColumnInfo(name = "latitude") override val latitude: Double,
    @ColumnInfo(name = "longitude") override val longitude: Double,
    @ColumnInfo(name = "description") override val description: String?,
    @ColumnInfo(name = "distance") override val distance: Int?,
    @ColumnInfo(name = "address") override val address: String?,
    @ColumnInfo(name = "wikipedia_info_json") override val wikipediaInfoJSON: String? = ""
) : POIData

@Entity(tableName = "poi_saved_table")
data class POISavedData(
    @PrimaryKey override val foursquareID: String,
    @ColumnInfo(name = "name") override val name: String,
    @ColumnInfo(name = "category") override val category: String,
    @ColumnInfo(name = "category_id") override val categoryID: String,
    @ColumnInfo(name = "time_triggered") override val timeTriggered: String,
    @ColumnInfo(name = "latitude") override val latitude: Double,
    @ColumnInfo(name = "longitude") override val longitude: Double,
    @ColumnInfo(name = "description") override val description: String?,
    @ColumnInfo(name = "distance") override val distance: Int?,
    @ColumnInfo(name = "address") override val address: String?,
    @ColumnInfo(name = "wikipedia_info_json") override val wikipediaInfoJSON: String? = ""
) : POIData

@Entity(tableName = "poi_history_table")
data class POIHistoryIds(
    @PrimaryKey override val foursquareID: String
) : Ids

@Entity(tableName = "poi_saved_table")
data class POISavedIds(
    @PrimaryKey override val foursquareID: String
) : Ids

@Entity(tableName = "category_table")
data class CategoryData(
    @PrimaryKey val foursquareID: String,
    @ColumnInfo val name: String,
    @ColumnInfo val likes: Int,
    @ColumnInfo val dislikes: Int
)