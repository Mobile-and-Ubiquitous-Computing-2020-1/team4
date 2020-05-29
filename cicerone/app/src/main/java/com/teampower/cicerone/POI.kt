package com.teampower.cicerone

import com.teampower.cicerone.wikipedia.WikipediaPlaceInfo

/**
 * Data class for POI object. May be unnecessary to have this alone in a file.
 * @param lat Latitude of POI
 * @param long Longitude of POI
 * @param id Unique ID of POI (for now this is the Foursquare ID)
 * @param name Name of teh POI
 * @param distance Current distance from POI in meters (at time of data retrieval)
 * @param address Address of the POI
 * @param category Category name for the POI
 * @param description Description string for the POI (description is retrieved from Wikipedia)
 */
data class POI(
    val id: String,
    val name: String,
    val category: String,
    val lat: Double,
    val long: Double,
    val description: String = "",
    val distance: Int?,
    val address: String?,
    var wikipediaInfo: WikipediaPlaceInfo? = null
)