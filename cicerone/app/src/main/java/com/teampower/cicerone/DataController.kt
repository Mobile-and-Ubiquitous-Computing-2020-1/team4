package com.teampower.cicerone

import android.content.Context
import android.widget.TextView
import android.util.Log
import com.google.android.gms.location.Geofence
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class DataController(private val geoCon: GeofencingController) {

    private val pois = mutableMapOf<String,POI>() // We can store all POIs in this map, indexed by ID

    fun requestData(location: android.location.Location, venue_view: TextView, mainContext: Context) {
        // Set context
        val context = mainContext

        // Loads client ID and secret from "secret.properties" file in BuildConfig
        val foursquare_id = BuildConfig.FOURSQUARE_ID
        val foursquare_secret = BuildConfig.FOURSQUARE_SECRET

        // API call parameters
        val location_string: String = "${location.latitude.toString()}, ${location.longitude.toString()}"
        val radius = 300 // TODO set radius for query
        val limit = 200
        // TODO decide which categories to query
        // comma-seperated list of Foursquare categoryIDs to query for
        val categories = "4d4b7104d754a06370d81259,4d4b7105d754a06373d81259,4d4b7105d754a06374d81259,4d4b7105d754a06376d81259,4d4b7105d754a06377d81259"
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
        FoursquareAPI.searchVenues(location_string, categories, radius, limit).enqueue(object : retrofit2.Callback<FoursquareData> {
            override fun onFailure(call: retrofit2.Call<FoursquareData>?, t: Throwable?) {
                Log.e(TAG, "Error: could not receive response from Foursquare API. ${t?.message}")

            }

            override fun onResponse(call: retrofit2.Call<FoursquareData>, response: retrofit2.Response<FoursquareData>) {
                if (response.isSuccessful()) {
                    val result = response.body()
                    val venues = result!!.response.venues
                    Log.d(TAG, "Venues:" + venues.toString())
                    for (venue in venues) {
                        val poi = poiBuilder(venue)
                        Log.d(TAG, "Venue:" + venue.toString())
                        // Create the geofence
                        val gf = geoCon.createGeofence(
                            poi.lat,
                            poi.long,
                            poi.id,
                            1F,
                            Geofence.NEVER_EXPIRE,
                            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
                        )
                        geoCon.addGeofence(gf, context, poi)
                        // Add POI to the list of current POIs
                        pois.put(poi.id, poi)
                    }
                    displayData(pois.toList().get(0).second, venue_view)
                }
            }
        })
    }

    private fun poiBuilder(venue: Venues): POI {
        val id = venue.id
        val name = venue.name
        val lat = venue.location.labeledLatLngs.get(0).lat
        val long = venue.location.labeledLatLngs.get(0).lng
        val distance = venue.location.distance
        val address = venue.location.formattedAddress.joinToString()
        var categories = ""
        for (cat in venue.categories) {
            categories = categories + cat.name
        }
        return POI(id, name, lat, long, distance, address, categories)
    }

    private fun displayData(poi: POI, venue_view: TextView) {
        val poi_string = StringBuilder()
        poi_string.append("Name: ${poi.name}").appendln()
        poi_string.append("Location: ${poi.lat}, ${poi.long}").appendln()
        poi_string.append("Address: ${poi.address}").appendln()
        poi_string.append("Category: ${poi.category}").appendln()
        poi_string.append("Current distance: ${poi.distance}m")
        venue_view.text = poi_string
    }

}
