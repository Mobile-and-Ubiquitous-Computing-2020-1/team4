package com.teampower.cicerone.control

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.TextView
import com.google.android.gms.location.Geofence
import com.teampower.cicerone.*
import com.teampower.cicerone.database.CategoryData
import com.teampower.cicerone.viewmodels.CategoryViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList


class DataController(private val geoCon: GeofencingController) {
    private val DATA_CON = "DataController"
    private lateinit var categoryScores : List<CategoryData>
    private lateinit var categoryViewModel: CategoryViewModel
    private val pois = mutableMapOf<String, POI>() // We can store all POIs in this map, indexed by ID
    private val uniformRandom = Random() // seed 1 - TODO remove seed


    fun requestData(location: android.location.Location, venue_view: TextView, mainContext: Context) {
        // Set context
        val context = mainContext

        // Loads client ID and secret from "secret.properties" file in BuildConfig
        val foursquare_id = BuildConfig.FOURSQUARE_ID
        val foursquare_secret =
            BuildConfig.FOURSQUARE_SECRET

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
            .addInterceptor(
                FoursquareRequestInterceptor(
                    foursquare_id,
                    foursquare_secret,
                    version,
                    cacheDuration
                )
            )
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
                    //Log.d(TAG, "Venues:" + venues.toString())
                    // For now, take the 100 closest POIs and make sure they aren't closer than 10 m
                    val closestVenues = getClosestVenues(venues)
                    val filteredVenues = filterVenues(closestVenues, 200F) // Remove POIs if closer 200m of each other - google recommends minimum radius of 100m
                    val radius = calculateRadius(filteredVenues)
                    Log.d(DATA_CON, "Radius: $radius m")
                    for ((id, venue) in filteredVenues.withIndex()) {
                        Log.d(DATA_CON, "ID: $id - Venue:" + venue.toString())
                        val poi = poiBuilder(venue, id)
                        // Create the geofence
                        val gf = geoCon.createGeofence(
                            poi.lat,
                            poi.long,
                            poi.id,
                            radius,
                            Geofence.NEVER_EXPIRE,
                            Geofence.GEOFENCE_TRANSITION_ENTER
                        )
                        geoCon.addGeofence(gf, context, poi)
                        // Add POI to the list of current POIs
                        pois.put(poi.id, poi)
                    }
                    if(!pois.isEmpty()){
                        displayData(pois.toList().get(0).second, venue_view)
                    }
                }
            }
        })
    }

    fun getPOI(id: String): POI? {
        return pois.get(id)
    }

    private fun poiBuilder(venue: Venues, id: Int): POI {
        // val id = venue.id # Replace this with a running id, to let Geofences be replaced
        val name = venue.name
        val lat = venue.location.lat
        val long = venue.location.lng
        val distance = venue.location.distance
        val address = venue.location.formattedAddress.joinToString()
        var categories = ""
        var categoryID = ""
        for (cat in venue.categories) {
            categories = categories + cat.name
            if(categoryID!= ""){
                categoryID = categoryID + ","  // Add delimiter between Ids
            }
            categoryID = categoryID + cat.id
        }
        return POI(
            id.toString(),
            name,
            lat,
            long,
            distance,
            address,
            categories,
            categoryID
        )
    }

    private fun displayData(poi: POI, venue_view: TextView) {
        val poi_string = StringBuilder()
        poi_string.append("Name: ${poi.name}").appendln()
        poi_string.append("Location: ${poi.lat}, ${poi.long}").appendln()
        poi_string.append("Address: ${poi.address}").appendln()
        poi_string.append("Category: ${poi.category}").appendln()
        poi_string.append("CategoryIDs: ${poi.categoryID}").appendln()
        poi_string.append("Current distance: ${poi.distance}m")
        venue_view.text = poi_string
    }

    private fun getClosestVenues(venues: List<Venues>): List<Venues> {
        val closestVenues = venues.sortedBy { venue -> venue.location.distance }
        if (closestVenues.size > 100 ){
            return closestVenues.slice(0..2)
        }
        return closestVenues.slice(0..2)
    }

    private fun calculateRadius(venues: List<Venues>): Float {
        var shortestDistance = 300F
        for (i in venues.indices) {
            for (j in i + 1 until venues.size) { // compare list.get(i) and list.get(j)
                val locationA = Location("A")
                locationA.latitude = venues[i].location.lat
                locationA.longitude = venues[i].location.lng
                val locationB = Location("B")
                locationB.latitude = venues[j].location.lat
                locationB.longitude = venues[j].location.lng

                val distance = locationA.distanceTo(locationB)
                if (distance < shortestDistance) {
                    shortestDistance = distance
                }
            }
        }
        return shortestDistance/2
    }

    private fun filterVenues(venues: List<Venues>, threshold: Float): ArrayList<Venues> {
        val filteredVenues = ArrayList<Venues>() // TODO don't use
        val indicesNotToAdd = ArrayList<Int>()
        for (i in venues.indices) {
            for (j in i + 1 until venues.size) {
                val venueA = venues[i]
                val venueB = venues[j]

                val locationA = Location("A")
                locationA.latitude = venueA.location.lat
                locationA.longitude = venueA.location.lng
                val locationB = Location("B")
                locationB.latitude = venueB.location.lat
                locationB.longitude = venueB.location.lng

                val distanceAB = locationA.distanceTo(locationB) // Distance between AB
                // Filter out the POI that's farthest away
                if( distanceAB < threshold) {
                    // Filtering based on cateogory score
                    val scoreA = fetchCategoryScore(venueA.categories)
                    val scoreB = fetchCategoryScore(venueB.categories)
                    val pA = scoreA/(scoreA+scoreB)
                    val rv = uniformRandom.nextDouble()
                    Log.i(DATA_CON, "pA=$pA")
                    Log.i(DATA_CON, "rv=$rv")
                    // TODO reimplement how we filter out the places that are not selected: this is not working atm.
                    // make a probabilistic choice
                    if(rv <= pA && !indicesNotToAdd.contains(j)){
                        // If chosen A, remove B
                        indicesNotToAdd.add(j)
                        Log.i(DATA_CON, "Venue ${venueB} removed")
                    }else if(rv > pA && !indicesNotToAdd.contains(i)) {
                        // if chosen B, remove A
                        indicesNotToAdd.add(i)
                    }
                }
            }
            Log.i(DATA_CON, "Indices not to add: $indicesNotToAdd")
            if (!indicesNotToAdd.contains(i)) {
                filteredVenues.add(venues[i])
            }
        }
        return filteredVenues
    }

    private fun fetchCategoryScore(categories: List<Categories>): Double {
        var score = 0.0
        for(catScore in categoryScores){
            for(cat in categories){
                if(catScore.foursquareID==cat.id){
                    score = catScore.score
                }
            }
        }
        if(score<1){
            // If category is not in databse, add that category to the database and initialize the score to 1
            // Or if decremented below pseudocount, increase to 1. Works due to replace strategy on insert.
            for(cat in categories){
                categoryViewModel.insert(CategoryData(cat.id, cat.name, 1.0))
                Log.i(DATA_CON, "Category ${cat.name} added to database")
            }
            score = 1.0
        }
        return score
    }

    fun setCategoryScores(cats: List<CategoryData>){
        categoryScores = cats
        Log.i(DATA_CON, "Category scores updated to: ${categoryScores}")
    }

    fun setCatViewModel(catViewModel: CategoryViewModel) {
        categoryViewModel = catViewModel
    }

}
