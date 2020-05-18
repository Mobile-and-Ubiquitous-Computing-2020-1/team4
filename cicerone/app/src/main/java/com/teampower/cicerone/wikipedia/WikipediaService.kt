package com.teampower.cicerone.wikipedia

import com.teampower.cicerone.wikipedia.WikipediaPlaceInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface WikipediaService {
    @GET("page/summary/{placeName}")
    fun getPageSummary(
        @Path("placeName") placeName: String
    ): Call<WikipediaPlaceInfo>
}