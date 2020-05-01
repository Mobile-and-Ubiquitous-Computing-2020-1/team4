package com.teampower.cicerone

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class RestAPI() {
    private val wikipediaService: WikipediaService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://en.wikipedia.org/api/rest_v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        wikipediaService = retrofit.create(WikipediaService::class.java)
    }

    fun getPlaceInfo(placeName: String): Call<WikipediaPlaceInfo> {
        return wikipediaService.getPageSummary(
            placeName = placeName
        )
    }
}