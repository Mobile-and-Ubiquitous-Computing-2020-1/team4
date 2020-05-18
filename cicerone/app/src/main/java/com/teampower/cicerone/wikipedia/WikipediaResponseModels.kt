package com.teampower.cicerone.wikipedia

import com.google.gson.internal.LinkedTreeMap

data class WikipediaPlaceInfo(
    val title: String,
    val thumbnail: Thumbnail,
    val description: String,
    val coordinates: Coordinates,
    val content_urls: Any,
    val extract: String
)

data class Thumbnail(
    val source: String,
    val width: Int,
    val height: Int
)

data class Coordinates(
    val lat: Float,
    val lon: Float
)
