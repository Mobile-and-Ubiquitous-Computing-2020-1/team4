package com.teampower.cicerone

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FoursquareAPI {
    @GET("/v2/venues/search")
    fun searchVenuesbyLocation(@Query("ll") location: String
    ): Call<FoursquareData>

    @GET("/v2/venues/search")
    fun searchVenues(@Query("ll") location: String,
                     @Query("categoryID") query: String,
                     @Query("radius") radius: Int,
                     @Query("limit") limit: Int
    ): Call<FoursquareData>
}