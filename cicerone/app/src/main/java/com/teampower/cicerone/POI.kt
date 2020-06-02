package com.teampower.cicerone

import com.teampower.cicerone.wikipedia.WikipediaPlaceInfo

/**
 * Data class for POI object. May be unnecessary to have this alone in a file.
 * @param lat Latitude of POI
 * @param long Longitude of POI
 * @param id Unique ID of POI (for now this is the Foursquare ID)
 * @param name Name of the POI
 * @param distance Current distance from POI in meters (at time of data retrieval)
 * @param address Address of the POI
 * @param category Category name for the POI
 * @param description Description string for the POI (description is retrieved from Wikipedia)
 */
data class POI(
    val id: String,
    val name: String,
    val lat: Double,
    val long: Double,
    val distance: Int?,
    val address: String?,
    val category: String,
    val categoryID: String,
    val description: String? = null,
    val rating: Double? = null,
    val hours: String? = null,
    val phone: String? = null,
    val facebook: String? = null,
    val twitter: String? = null,
    val ig: String? = null,
    val photo_url: String? = null,
    val website: String? = null,
    val tip: String? = null,
    var wikipediaInfo: WikipediaPlaceInfo? = null
)