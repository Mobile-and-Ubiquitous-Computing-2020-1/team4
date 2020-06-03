package com.teampower.cicerone.control

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.Geofence
import com.teampower.cicerone.*
import com.teampower.cicerone.database.CategoryData
import com.teampower.cicerone.database.POIData
import com.teampower.cicerone.viewmodels.CategoryViewModel
import okhttp3.OkHttpClient
import okhttp3.internal.toImmutableList
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt
import kotlin.random.Random.Default.nextDouble

class DataController(private val geoCon: GeofencingController) {
    private val DATA_CON = "DataController"
    private lateinit var categoryTable: ArrayList<CategoryData>
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var poiHistory: List<POIData>
    private val pois =
        mutableMapOf<String, POI>() // We can store all POIs in this map, indexed by ID
    private val uniformRandom = Random(1) // seed 1 - TODO remove seed

    fun requestData(
        location: android.location.Location,
        mainContext: Context
    ) {
        // Set context
        val context = mainContext

        // Loads client ID and secret from "secret.properties" file in BuildConfig
        val foursquare_id = BuildConfig.FOURSQUARE_ID
        val foursquare_secret = BuildConfig.FOURSQUARE_SECRET

        // API call parameters
        val location_string: String =
            "${location.latitude.toString()}, ${location.longitude.toString()}"
        val radius = 30
        val limit = 50 // Foursquare API returns up to 50 results
        // comma-separated list of Foursquare categoryIDs to query for
        val categories = "4bf58dd8d48988d181941735,4d4b7105d754a06374d81259,4bf58dd8d48988d116941735,50327c8591d4c4b30a586d5d,4bf58dd8d48988d1e2941735,4bf58dd8d48988d163941735,52e81612bcbc57f1066b7a14,50aaa49e4b90af0d42d5de11,4bf58dd8d48988d164941735,4bf58dd8d48988d1e2931735,56aa371be4b08b9a8d573532,52e81612bcbc57f1066b7a22,56aa371be4b08b9a8d573562,56aa371be4b08b9a8d573544,4eb1d4dd4b900d56c88a45fd,4bf58dd8d48988d133951735,4bf58dd8d48988d165941735,4bf58dd8d48988d12f941735"
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
        FoursquareAPI.searchVenues(location_string, categories, radius, limit)
            .enqueue(object : retrofit2.Callback<FoursquareData> {
                override fun onFailure(call: retrofit2.Call<FoursquareData>?, t: Throwable?) {
                    Log.e(
                        TAG,
                        "Error: could not receive response from Foursquare API. ${t?.message}"
                    )

                }

                override fun onResponse(
                    call: retrofit2.Call<FoursquareData>,
                    response: retrofit2.Response<FoursquareData>
                ) {
                    if (response.isSuccessful()) {
                        val result = response.body()
                        val venues = result!!.response.venues
                        val filteredVenues = filterVenuesPOIHistory(venues)
                        initializeCategories(filteredVenues)
                        val recommendVenues = recommendVenues(venues, 50)
                        val nonOverlappingVenues = filterOverlapGreedy(
                            recommendVenues.toMutableList(),
                            200F
                        ) // Remove POIs if closer 200m of each other - google recommends minimum radius of 100m
                        val radius = calculateRadius(nonOverlappingVenues.toTypedArray())
                        Log.d(DATA_CON, "Radius: $radius m")
                        for ((id, venue) in nonOverlappingVenues.withIndex()) {
                            val id = venue?.id
                            Log.d(DATA_CON, "ID: $id - Venue:" + venue.toString())
                            val poi = poiBuilder(venue!!)
                            // Create the geofence
                            val gf = geoCon.createGeofence(
                                poi.lat,
                                poi.long,
                                id.toString(),
                                radius,
                                Geofence.NEVER_EXPIRE,
                                Geofence.GEOFENCE_TRANSITION_ENTER
                            )
                            geoCon.addGeofence(gf, context, poi)
                            // Add POI to the list of current POIs
                            pois.put(poi.id, poi)
                        }
                    }
                }
            })
    }

    private fun poiBuilder(venue: Venues): POI {
        // val id = venue.id # Replace this with a running id, to let Geofences be replaced
        val id = venue.id
        val name = venue.name
        val lat = venue.location.lat
        val long = venue.location.lng
        val distance = venue.location.distance
        val address = venue.location.formattedAddress.joinToString()
        var categories = ""
        var categoryID = ""
        for (cat in venue.categories) {
            categories += cat.name
            if (categoryID != "") {
                categoryID += ","  // Add delimiter between Ids
            }
            categoryID += cat.id
        }
        return POI(
            id = id,
            name = name,
            lat = lat,
            long = long,
            distance = distance,
            address = address,
            category = categories,
            categoryID = categoryID
        )
    }

    /**
     * Recommend POIs based on the user's preference.
     * Uses a Bayesian approach, with the following definitions:
     *      E_i: The event that the POI belongs to category i
     *      M_j: User likes POI j. Either the user likes it or not
     *
     * We seek P(M_j|E_i) = P(liked j | category i). This can be inferred as (Bayes inference formula):
     *      P(Liked|Category) = P(Category|Liked) * P(Liked) / (P(Category|Liked) * P(Liked) + P(Category|Disliked) * P(Disliked))
     *
     * The likelihood P(Category|Liked) is computed from the fraction of likes for that category.
     * The prior P(liked) is set to flat 0.5 at the moment (may be tuned later).
     */
    private fun recommendVenues(venues: List<Venues>, N: Int): Array<Venues?> {
        val mutableVenues = venues.toMutableList()
        val recommendedVenues = arrayOfNulls<Venues>(minOf(N, venues.size))
        val posterior = arrayOfNulls<Double>(venues.size)
        val fp = 0.5  // flat prior
        // 1. Perform inference of posterior probability for each POI
        for (i in venues.indices) {
            // TODO what to do with places having multiple categories? For now only handle single.
            var likes = 0.0
            var dislikes = 0.0
            var categoryID = ""
            // Some POIs have no registered category. Then set the probability to 50% that the
            // user likes the POI.
            if (venues[i].categories.isEmpty()) {
                likes = 1.0
                dislikes = 1.0
            } else {
                categoryID = venues[i].categories[0].id
            }

            for (cat in categoryTable) {
                if (cat.foursquareID == categoryID) {
                    likes = cat.likes.toDouble()
                    dislikes = cat.dislikes.toDouble()
                }
            }
            val ll = likes / (likes + dislikes) // Likelihood P(Category i | Liked)
            val ld = dislikes / (likes + dislikes)
            val pp = ll * fp / (ll * fp + ld * fp)
            posterior[i] = pp
            /* DEBUG
            if(i<2){
                Log.i(DATA_CON, "Category: ${venues[i].categories[0].name}, Likes: $likes, dislikes: $dislikes, l_l: $ll, d_l: $ld, posterior: ${pp}")
            }
            */
        }
        // 2. Select a subset of size N of the venues based on their posterior probabilities
        // Normalize posterior
        var normPosterior = normalize(posterior.toMutableList())
        for (i in recommendedVenues.indices) {
            /* Draw a random POI. Do this by drawing a random value between 0 and 1, finding when
               this value is smaller than the cumulative sum of posteriors.

               Example: post = [0.2, 0.5, 0.3]. rand = 0.4
               rand > 0.2 but less than 0.7. Thus it falls in the bin of size 0.5 corresponding to
               the second element.
            */
            val urv = uniformRandom.nextDouble()
            var cumSum = 0.0
            var ind = 0
            while (cumSum < urv) {
                cumSum += normPosterior[ind]!!
                ind += 1
            }
            if (ind == mutableVenues.size) {
                // Sometimes the criteria isn't satisfied due to rounding errors (?). Wrap-in
                // to the last element in that case.
                ind -= 1
            }
            recommendedVenues[i] = mutableVenues[ind] // Add the chosen POI
            // Remove the added elements
            mutableVenues.removeAt(ind)
            normPosterior.removeAt(ind)
            // Re-normalize posterior
            normPosterior = normalize(normPosterior)
        }
        return recommendedVenues
    }

    private fun normalize(l: MutableList<Double?>): MutableList<Double?> {
        var nl = l.toMutableList()
        var sum = 0.0
        for (x in l) {
            if (x != null) {
                sum += x
            }
        }
        for (i in l.indices) {
            nl[i] = l[i]?.div(sum)
        }
        return nl
    }

    /**
     * Handle overlapping POIs by greedily choosing the POI with the highest score (likes-dislikes)
     * in the case of conflict. If the score happens to be the same, chose a random POI.
     *
     * Recursive algorithm that handles overlaps one at a time until none remains. The approach
     * is chosen to simplify the code, and to get around the problem of one POI overlaping
     * multiple others.
     */
    private fun filterOverlapGreedy(
        venues: MutableList<Venues?>,
        threshold: Float
    ): MutableList<Venues?> {
        //Log.i(DATA_CON, "Greedy: size=${venues.size}")
        var mutableVenues = venues.toMutableList()
        val overlappingIndices = arrayOfNulls<Int>(2)
        var overlapFound = false
        var i = 0
        while (i < venues.size && !overlapFound) {
            var j = i + 1
            while (j < venues.size && !overlapFound) {
                val venueA = venues[i]
                val venueB = venues[j]

                val locationA = Location("A")
                locationA.latitude = venueA!!.location.lat
                locationA.longitude = venueA.location.lng
                val locationB = Location("B")
                locationB.latitude = venueB!!.location.lat
                locationB.longitude = venueB.location.lng

                val distanceAB =
                    locationA.distanceTo(locationB) // Distance between AB
                if (distanceAB < threshold) {
                    overlappingIndices[0] = i
                    overlappingIndices[1] = j
                    overlapFound = true
                    //Log.i(DATA_CON, "\tOverlap found at i=$i j=$j with distance $distanceAB m")
                }
                j += 1
            }
            i += 1
        }
        if (overlapFound) {
            val i = overlappingIndices[0]!!
            val j = overlappingIndices[1]!!
            val scoreA = fetchCategoryScore(venues[i]!!)
            val scoreB = fetchCategoryScore(venues[j]!!)
            //Log.i(DATA_CON, "Overlap between: ${venues[i]!!.name}, score = $scoreA and ${venues[j]!!.name} score = $scoreB")
            // Kinda boring but everything has score 0 atm since no likes and too many categories.
            // TODO: Maybe initialize DB better to get more plausible recommendations?
            if (scoreA > scoreB) {
                mutableVenues.removeAt(j)
            } else if (scoreA < scoreB) {
                mutableVenues.removeAt(i)
            } else {
                // make a random choice which to delete if they are the same
                val x = nextDouble().roundToInt() // either 0 or 1
                mutableVenues.removeAt(overlappingIndices[x]!!)
            }
            mutableVenues = filterOverlapGreedy(mutableVenues, threshold)
        } else {
            // Log.i(DATA_CON, "No overlaps found!")
        }
        return mutableVenues
    }

    /**
     * Category score = likes-dislikes
     */
    private fun fetchCategoryScore(venue: Venues): Int {
        var score = 0
        if (!venue.categories.isEmpty()) {
            var categoryID = venue.categories[0].id
            for (cat in categoryTable) {
                // Some POIs have no registered category. Then set their score to 0.
                if (cat.foursquareID == categoryID) {
                    score = cat.likes - cat.dislikes
                }
            }
        }
        return score
    }

    private fun calculateRadius(venues: Array<Venues?>): Float {
        var shortestDistance = 300F
        for (i in venues.indices) {
            for (j in i + 1 until venues.size) { // compare list.get(i) and list.get(j)
                val locationA = Location("A")
                locationA.latitude = venues[i]!!.location.lat
                locationA.longitude = venues[i]!!.location.lng
                val locationB = Location("B")
                locationB.latitude = venues[j]!!.location.lat
                locationB.longitude = venues[j]!!.location.lng

                val distance = locationA.distanceTo(locationB)
                if (distance < shortestDistance) {
                    shortestDistance = distance
                }
            }
        }
        return shortestDistance / 2
    }

    private fun initializeCategories(venues: List<Venues>) {
        // Check the DB for an entry for each category for each venue in the list of venues.
        // If a category doesn't exist, initializes it with likes=1, dislikes=1
        for (v in venues) {
            for (cat in v.categories) {
                var existInDB = false
                for (dBCat in categoryTable) {
                    // This will be true if cat matches once in the database, which is what we want to check
                    //Log.i(DATA_CON, "\tcat.id==dBcat.foursquareID: ${cat.id}===${dBCat.foursquareID} = ${dBCat.foursquareID == cat.id}")
                    existInDB = existInDB || dBCat.foursquareID == cat.id
                }
                if (!existInDB) {
                    categoryViewModel.insert(CategoryData(cat.id, cat.name, 1, 1))
                    categoryTable.add(CategoryData(cat.id, cat.name, 1, 1))
                    Log.i(DATA_CON, "Added Category ${cat.name} to database.")
                }
            }
        }
    }

    /**
     * Filter out POIs that are in the history, i.e. have been recommended before
     */
    private fun filterVenuesPOIHistory(venues: List<Venues>): List<Venues> {
        val filteredVenues = ArrayList<Venues>()
        for (venue in venues){
            var inHistory = false
            for ( poiH in poiHistory ){
                inHistory = inHistory || poiH.foursquareID == venue.id
            }
            if (inHistory) {
                Log.i(DATA_CON, "Venue ${venue.name} is already in the history. Skipping")
            } else{
                filteredVenues.add(venue)
            }
        }
        return filteredVenues.toImmutableList()
    }

    fun setCategoryScores(cats: List<CategoryData>) {
        categoryTable = ArrayList(cats)
        //Log.i(DATA_CON, "Category scores set to: ${categoryTable}")
    }

    fun setCatViewModel(catViewModel: CategoryViewModel) {
        categoryViewModel = catViewModel
    }

    fun setPOIHistory(pois: List<POIData>){
        poiHistory = pois
    }

}