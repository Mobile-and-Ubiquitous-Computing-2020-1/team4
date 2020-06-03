package com.teampower.cicerone

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path
import com.teampower.cicerone.foursquare.premium.FoursquarePremiumData

interface FoursquareAPI {
    @GET("/v2/venues/search")
    fun searchVenues(@Query("ll") location: String,
                     @Query("categoryId") query: String,
                     @Query("radius") radius: Int,
                     @Query("limit") limit: Int
    ): Call<FoursquareData>

    @GET("/v2/venues/{venueID}")
    fun getVenueDetails(@Path("venueID") venueID: String
    ): Call<FoursquarePremiumData>
}