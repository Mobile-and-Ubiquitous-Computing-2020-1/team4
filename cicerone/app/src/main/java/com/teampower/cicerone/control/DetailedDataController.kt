package com.teampower.cicerone.control

import android.content.Context
import android.location.Location
import android.text.Html
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.squareup.picasso.Picasso
import com.teampower.cicerone.*
import com.teampower.cicerone.foursquare.premium.FoursquarePremiumData
import com.teampower.cicerone.foursquare.premium.Venue
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.round

class DetailedDataController() {
    fun requestVenueDetails(venueID: String, venue_general_view: TextView, venue_detail_view: TextView, tip_label_view: TextView, tip_view: CardView, map_link: TextView, venue_image_view: ImageView, context: Context) {

        // Loads client ID and secret from "secret.properties" file in BuildConfig
        val foursquare_id = BuildConfig.FOURSQUARE_ID
        val foursquare_secret = BuildConfig.FOURSQUARE_SECRET

        // API call parameters
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
        FoursquareAPI.getVenueDetails(venueID)
            .enqueue(object : retrofit2.Callback<FoursquarePremiumData> {
                override fun onFailure(
                    call: retrofit2.Call<FoursquarePremiumData>?,
                    t: Throwable?
                ) {
                    Log.e(
                        TAG,
                        "Error: could not receive response from Foursquare API. ${t?.message}"
                    )

                }
                override fun onResponse(
                    call: retrofit2.Call<FoursquarePremiumData>,
                    response: retrofit2.Response<FoursquarePremiumData>
                ) {
                    if (response.isSuccessful()) {
                        val result = response.body()
                        val venue = result!!.response.venue
                        val poi = poiBuilder(venue, venueID)
                        displayData(
                            poi,
                            venue_general_view,
                            venue_detail_view,
                            tip_label_view,
                            tip_view,
                            map_link,
                            venue_image_view,
                            context
                        )
                    }
                }
            })
    }

    private fun poiBuilder(venue: Venue, id: String?, user_location: android.location.Location? = null): POI {
        val name = venue.name
        val lat = venue.location.lat
        val long = venue.location.lng
        val address = venue.location?.formattedAddress.joinToString()
        val categories = venue.categories.joinToString { it.name }
        val categoryID = venue.categories.joinToString { it.id }
        val description = venue?.description
        val rating = venue?.rating
        val hours = venue.hours?.status
        val phone = venue.contact?.formattedPhone
        val facebook = venue.contact?.facebook
        val twitter = venue.contact?.twitter
        val ig = venue.contact?.instagram
        val photo = venue.bestPhoto?.prefix + "original" + venue.bestPhoto?.suffix // TODO: instead of original size, set the photo dimensions that we want to retrieve
        val website = venue?.url
        val tip = venue.tips.groups.getOrNull(0)?.items?.getOrNull(0)?.text

        // Compute distance to the venue if we passed the user location
        var distance: Int? = null
        user_location?.let {
            val venue_location = Location("")
            venue_location.latitude = lat
            venue_location.longitude = long
            distance = round(user_location.distanceTo(venue_location)).toInt()
        }

        // Create and return the POI object
        return POI(
            id = id.toString(),
            name = name,
            lat = lat,
            long = long,
            distance = distance,
            address = address,
            category = categories,
            categoryID = categoryID,
            description = description,
            rating = rating,
            hours = hours,
            phone = phone,
            facebook = facebook,
            twitter = twitter,
            ig = ig,
            photo_url = photo,
            website = website,
            tip = tip
        )
    }

    private fun displayData(poi: POI, venue_general_view: TextView, venue_detail_view: TextView, tip_label_view: TextView, tip_view: CardView, map_link: TextView, venue_image_view: ImageView, context: Context) {
        val generalDesc = StringBuilder()
        val detailDesc = StringBuilder()

        // Generate string with basic POI information
        generalDesc.append("Category: ${poi.category}")
        poi.rating?.let {
            generalDesc.appendln()
            generalDesc.append("Rating: ${poi.rating}\uD83C\uDF1F")
        }
        poi.description?.let {
            generalDesc.appendln()
            generalDesc.appendln()
            generalDesc.append("${poi.description}")
        }

        // Generate string with detailed POI information if it is available
        detailDesc.append("Address: ${poi.address}")
        poi.distance?.let {
            detailDesc.appendln()
            detailDesc.append("Current distance: ${poi.distance}m")
        }
        poi.hours?.let {
            detailDesc.appendln()
            detailDesc.append("Opening hours: ${poi.hours}")
        }
        poi.phone?.let {
            detailDesc.appendln()
            detailDesc.append("Phone number: ${poi.phone}")
        }
        poi.website?.let {
            detailDesc.appendln()
            detailDesc.append("Website: ${poi.website}")
        }
        poi.ig?.let {
            detailDesc.appendln()
            detailDesc.append("Instagram: https://instagram.com/${poi.ig}")
        }

        // Finally set the text view string to the POI description we generated above
        venue_general_view.text = generalDesc
        venue_detail_view.text = detailDesc
        venue_detail_view.removeLinkStyle()

        // Show "did you know" string if information is available
        poi.tip?.let {
            val linear_layout = tip_view.getChildAt(0) as LinearLayout
            val content_view = linear_layout.getChildAt(0) as TextView
            content_view.text = poi.tip
            tip_view.visibility = View.VISIBLE
            tip_label_view.visibility = View.VISIBLE
        }

        // Set the Google Maps link
        map_link.isClickable = true
        map_link.movementMethod = LinkMovementMethod.getInstance()
        val link_text = "<a href='https://maps.google.com/?daddr=${poi.lat},${poi.long}'> Get directions \uD83D\uDDFA </a>"
        map_link.text = Html.fromHtml(link_text, Html.FROM_HTML_MODE_COMPACT)
        map_link.removeLinkStyle()
        map_link.visibility = View.VISIBLE

        Log.d(TAG, "Retrieved photo: ${poi.photo_url}")

        // Set the image view to the POI's image that we have retrieved
        poi.photo_url?.let {
            Picasso.with(context)
                .load(poi.photo_url)
                .fit()
                .centerCrop()
                .error(R.drawable.toolbar_bg)
                .into(venue_image_view)
        }
    }

    private fun TextView.removeLinkStyle() {
        val spannable = SpannableString(text)
        for (u in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
            spannable.setSpan(object : URLSpan(u.url) {
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }, spannable.getSpanStart(u), spannable.getSpanEnd(u), 0)
        }
        text = spannable
    }
}