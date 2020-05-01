package com.teampower.cicerone

import android.widget.TextView
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class DataController {
   fun requestData(location: android.location.Location, venue_view: TextView) {
        // Loads client ID and secret from "secret.properties" file in BuildConfig
        val foursquare_id = BuildConfig.FOURSQUARE_ID
        val foursquare_secret = BuildConfig.FOURSQUARE_SECRET

        // API call parameters
        val location_string: String = "${location.latitude.toString()}, ${location.longitude.toString()}"
        val radius = "100"
        val limit = "30"
        val version = "20200420" // set date for API versioning here (see Foursquare API)
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
        FoursquareAPI.searchVenues(location_string).enqueue(object : retrofit2.Callback<FoursquareData> {
            override fun onFailure(call: retrofit2.Call<FoursquareData>?, t: Throwable?) {
                Log.e(TAG, "Error: could not receive response from Foursquare API")
            }

            override fun onResponse(call: retrofit2.Call<FoursquareData>, response: retrofit2.Response<FoursquareData>) {
                if (response.isSuccessful()) {
                    val result = response.body()
                    val venues = result!!.response.venues
                    val place = placeBuilder(venues.get(0))
                    Log.d("TAG", "Venues:" + venues.toString())
                    displayData(place, venue_view)
                }
            }
        })
    }

    private fun placeBuilder(venue: Venues): Place {
        val name = venue.name
        val latitude = venue.location.labeledLatLngs.get(0).lat
        val longitude = venue.location.labeledLatLngs.get(0).lng
        val distance = venue.location.distance
        val address = venue.location.formattedAddress.joinToString()
        val description = venue.categories.get(0).name
        return Place(name, latitude, longitude, distance, address, description)
    }

    private fun displayData(place: Place, venue_view: TextView) {
        val place_string = StringBuilder()
        place_string.append("Name: ${place.name}").appendln()
        place_string.append("Location: ${place.latitude}, -122.084000").appendln()
        place_string.append("Address: ${place.address}").appendln()
        place_string.append("Category: ${place.category}").appendln()
        place_string.append("Current distance: ${place.distance}m")
        venue_view.text = place_string
    }

}