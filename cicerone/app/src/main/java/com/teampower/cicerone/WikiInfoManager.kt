package com.teampower.cicerone

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WikiInfoManager(private val api: RestAPI = RestAPI()) {
    fun getSearchResults(searchString: String): List<WikipediaSearchQueryPageItem>? {
        var searchResults: List<WikipediaSearchQueryPageItem>? = null
        api.getSearchResult(searchString)
            .enqueue(object : Callback<WikipediaSearchResponse> {
                override fun onResponse(
                    call: Call<WikipediaSearchResponse>,
                    response: Response<WikipediaSearchResponse>
                ) {
                    searchResults = response.body()?.query?.search?.map {
                        WikipediaSearchQueryPageItem(
                            it.title, it.pageid, it.snippet
                        )
                    }
                    println("MEMEMEMEMEMEMEMEME: $searchResults")
                }

                override fun onFailure(call: Call<WikipediaSearchResponse>, t: Throwable) =
                    t.printStackTrace()
            })
        return searchResults
    }
}