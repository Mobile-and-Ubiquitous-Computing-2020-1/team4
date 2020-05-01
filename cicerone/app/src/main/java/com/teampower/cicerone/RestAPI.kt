package com.teampower.cicerone

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class RestAPI() {
    private val wikipediaService: WikipediaService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://en.wikipedia.org/w/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        wikipediaService = retrofit.create(WikipediaService::class.java)
    }

    fun getSearchResult(searchString: String): Call<WikipediaSearchResponse> {
        return wikipediaService.generalSearch(
            action = "query",
            list = "search",
            srsearch = searchString,
            format = "json"
        )
    }
}