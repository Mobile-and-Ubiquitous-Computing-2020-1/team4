package com.teampower.cicerone

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FoursquareAPI {
    @GET("/v2/venues/search")
    fun searchVenues(@Query("ll") location: String
    ): Call<FoursquareData>

    @GET("/v2/venues/search")
    fun searchVenuesQuery(@Query("ll") location: String,
                          @Query("radius") radius: Int,
                          @Query("query") query: String,
                          @Query("limit") limit: Int
    ): Call<FoursquareData>
}