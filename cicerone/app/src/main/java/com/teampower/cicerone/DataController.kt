package com.teampower.cicerone

import android.content.Context
import android.widget.TextView
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DataController {
    fun placeBuilder(venue: Venues): Place {
        val name = venue.name
        val latitude = venue.location.labeledLatLngs.get(0).lat
        val longitude = venue.location.labeledLatLngs.get(0).lng
        val distance = venue.location.distance
        val address = venue.location.formattedAddress.joinToString()
        val description = venue.categories.get(0).name
        return Place(name, latitude, longitude, distance, address, description)
    }

    fun requestData(context: Context, venue_description: TextView) {
        // Loads client ID and secret from "secret.properties" file in BuildConfig
        val foursquare_id = BuildConfig.FOURSQUARE_ID
        val foursquare_secret = BuildConfig.FOURSQUARE_SECRET

        // API call parameters
        val location = "38.8897,-77.0089"
        val radius = "100"
        val limit = "30"
        val version = "20200420" // set date for API versioning here
        val cacheDuration = 60

        // Set up HTTP client
        val client = OkHttpClient().newBuilder()
            .addInterceptor(FoursquareRequestInterceptor(foursquare_id, foursquare_secret, version, cacheDuration))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
            })
            .build()

        // Set up retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.foursquare.com")
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Make API call
        val FoursquareAPI = retrofit.create(FoursquareAPI::class.java)
        var result: FoursquareData? = null

        FoursquareAPI.searchVenues(location).enqueue(object : Callback<FoursquareData> {
            override fun onFailure(call: Call<FoursquareData>?, t: Throwable?) {
                Log.e("error", "")
            }

            override fun onResponse(call: Call<FoursquareData>, response: retrofit2.Response<FoursquareData>) {
                if (response.isSuccessful()) {
                    result = response.body()
                    val venues = result!!.response?.venues
                    val place = placeBuilder(venues?.get(0))
                    Log.d("TAG", "Venues:" + venues?.toString())
                    displayData(place, venue_description)
                }
            }
        })
        /*
        val description = "the united states capitol, often called the capitol building, is the home of the united states congress and the seat of the legislative branch of the u.s. federal government. it is located on capitol hill at the eastern end of the national mall in washington, d.c."
        var place = Place("united states capitol", 38.8897, -77.0089, "first st se, washington, dc 20004, united states", description)
        return place
         */
    }

    fun displayData(place: Place, venue_description: TextView) {
        val place_string = StringBuilder()
        place_string.append("Name: ${place.name}").appendln()
        place_string.append("Location: ${place.longitude}, ${place.latitude}").appendln()
        place_string.append("Address: ${place.address}").appendln()
        place_string.appendln()
        place_string.append(place.description)

        venue_description.text = place_string
    }

}