package com.teampower.cicerone

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WikiInfoManager(private val api: RestAPI = RestAPI()) {
    fun getPlaceInfo(placeName: String): WikipediaPlaceInfo? {
        var placeInfo: WikipediaPlaceInfo? = null
        api.getPlaceInfo(placeName)
            .enqueue(object : Callback<WikipediaPlaceInfo> {
                override fun onResponse(
                    call: Call<WikipediaPlaceInfo>,
                    response: Response<WikipediaPlaceInfo>
                ) {
                    placeInfo = response.body()

                    println("MEMEMEMEMEMEMEMEME: $placeInfo")
                }

                override fun onFailure(call: Call<WikipediaPlaceInfo>, t: Throwable) =
                    t.printStackTrace()
            })
        return placeInfo
    }
}