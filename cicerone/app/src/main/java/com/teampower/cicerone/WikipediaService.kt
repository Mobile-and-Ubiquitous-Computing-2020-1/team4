package com.teampower.cicerone

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WikipediaService {
    @GET("api.php")
    fun generalSearch(
        @Query("action") action: String,
        @Query("list") list: String,
        @Query("srsearch") srsearch: String,
        @Query("format") format: String
    ): Call<WikipediaSearchResponse>

    @GET("api.php")
    fun getWikiPage(
        @Query("action") action: String,
        @Query("list") list: String,
        @Query("srsearch") srsearch: String,
        @Query("format") format: String
    ): Call<WikipediaSearchResponse>
}