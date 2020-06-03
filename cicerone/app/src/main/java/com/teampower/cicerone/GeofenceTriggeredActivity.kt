package com.teampower.cicerone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Html
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import com.google.gson.internal.LinkedTreeMap
import com.teampower.cicerone.control.DetailedDataController
import com.teampower.cicerone.viewmodels.CategoryViewModel
import com.teampower.cicerone.viewmodels.POIHistoryViewModel
import com.teampower.cicerone.viewmodels.POISavedViewModel
import kotlinx.android.synthetic.main.activity_geofence_triggered.*
import kotlinx.android.synthetic.main.activity_scrolling.toolbar
import kotlinx.android.synthetic.main.content_geofence_triggered.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

const val POI_DETAILS = "POI_DETAILS"


class GeofenceTriggeredActivity : AppCompatActivity() {
    private val TRIG_TAG = "POIActivity"
    private lateinit var catViewModel: CategoryViewModel
    private lateinit var poiSavedViewModel: POISavedViewModel
    private lateinit var poiHistoryViewModel: POIHistoryViewModel
    lateinit var tts: TextToSpeech
    private var speaking = false
    private var tts_text = ""
    private var isSaved = false
    private var detailCon = DetailedDataController()

    companion object {
        // Whenever we want to create this Activity, we use it via this intent creation function.
        fun getStartIntent(context: Context, poiObjectJSON: String): Intent {
            return Intent(context, GeofenceTriggeredActivity::class.java)
                .putExtra(POI_DETAILS, poiObjectJSON)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofence_triggered)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Home button to MainActivity

        poiSavedViewModel = ViewModelProvider(this).get(POISavedViewModel::class.java)
        poiHistoryViewModel = ViewModelProvider(this).get(POIHistoryViewModel::class.java)
        // Extract the transitionDetails
        val poiObjectJSON = intent.getStringExtra(POI_DETAILS) ?: ""
        val poi = MainActivity.fromJson<POI>(poiObjectJSON)
        MainScope().launch {
            val result = poiSavedViewModel.loadPOI(poi.id).await()
            isSaved = result !== null
            if (isSaved) {
                DrawableCompat.setTint(
                    DrawableCompat.wrap(favorite_button.drawable),
                    ContextCompat.getColor(applicationContext, R.color.yellow)
                )
            }
        }

        // Save the recommended POI to the history - added here since GeofenceBroadCastReceiver is not a viewModelStoreOwner
        poiHistoryViewModel.insert(poi)

        Log.d(TAG, "Requesting detailed data from Foursquare")
        // Request detailed data for the POI from Foursquare in a non-blocking manner
        GlobalScope.launch {
            // Get the current location
            detailCon.requestVenueDetails(
                poi.id,
                shortFactsContent,
                detailedFactsContent,
                tipCardLabel,
                tipCardView,
                shortFactsMapLink,
                venueImageView,
                this@GeofenceTriggeredActivity
            )
            Log.d(TAG, "Finished retrieving data from Foursquare")
        }


        // Update the view
        title = poi.name
        shortFactsPlaceName.text = poi.name
        location_category.text = this.getString(
            R.string.location_category,
            poi.category
        )
        location_distance.text = this.getString(
            R.string.location_distance,
            poi.distance.toString()
        )
        if (poi.wikipediaInfo != null) {
            // For some reason didn't work to set proper type in WikipediaResponseModule.kt
            val url_list: LinkedTreeMap<String, LinkedTreeMap<String, String>> =
                poi.wikipediaInfo?.content_urls as LinkedTreeMap<String, LinkedTreeMap<String, String>>
            Log.i(TRIG_TAG, "${url_list::class.simpleName}")
            Log.i(TRIG_TAG, "${url_list["mobile"]?.get("page")}")
            // It's possible to add "From Wikipedia" using R.string.location_description_yes_wikipedia to the String below, but kind of redundant since the link implies this anyway
            /*wikipediaExtract.text = this.getString(
                R.string.location_description_yes_wikipedia,
                poi.wikipediaInfo?.extract
            )*/
            wikipediaExtract.text = poi.wikipediaInfo?.extract
            wikipediaArticleLink.isClickable = true
            wikipediaArticleLink.movementMethod = LinkMovementMethod.getInstance()
            val text =
                "<a href='${url_list["mobile"]?.get("page")}'> Read more on Wikipedia </a>"
            wikipediaArticleLink.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
            wikipediaArticleLink.removeLinkStyle()
            wikipediaArticleLink.visibility = View.VISIBLE
            tts_text = this.getString(
                R.string.tts_text_yes_wikipedia,
                poi.name,
                poi.category,
                poi.distance,
                poi.wikipediaInfo?.extract
            )
        } else {
            wikipediaExtract.text = this.getString(
                R.string.location_description_no_wikipedia
            )
            tts_text = this.getString(
                R.string.tts_text_no_wikipedia,
                poi.name,
                poi.category,
                poi.distance
            )
        }


        tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                // setting the language to the default phone language.
                val ttsLang = tts.setLanguage(Locale.getDefault())
                // check if the language is supportable.
                if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "We can't support your language", Toast.LENGTH_LONG).show()
                    Log.i(TRIG_TAG, "Default language not supported")
                }
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String?) {
                        speaking = false
                        Log.i(TRIG_TAG, "TTS of text completed")
                    }

                    override fun onError(utteranceId: String?) {
                        speaking = false
                        Log.i(TRIG_TAG, "TTS of text failed")
                    }

                    override fun onStart(utteranceId: String?) {
                        speaking = true
                        Log.i(TRIG_TAG, "TTS of text started")
                    }
                })
                Log.i(TRIG_TAG, "TTS Initialization completed")
            } else {
                Log.i(TRIG_TAG, "TTS Initialization failed")
            }
        })
        ttsButton.setOnClickListener {
            if (speaking) {
                tts.stop()
                speaking = false
                ttsButton.setImageResource(R.drawable.ic_play_arrow_black_32dp)
                DrawableCompat.setTint(
                    DrawableCompat.wrap(ttsButton.drawable),
                    ContextCompat.getColor(applicationContext, android.R.color.white)
                )
            } else {
                tts.speak(tts_text, TextToSpeech.QUEUE_FLUSH, null, "TRIG_TTS")
                ttsButton.setImageResource(R.drawable.ic_stop_black_32dp)
                DrawableCompat.setTint(
                    DrawableCompat.wrap(ttsButton.drawable),
                    ContextCompat.getColor(applicationContext, android.R.color.white)
                )
            }
        }

        favorite_button.setOnClickListener { toggleFavoritePOI(poi) }
        DrawableCompat.setTint(
            DrawableCompat.wrap(favorite_button.drawable),
            ContextCompat.getColor(applicationContext, android.R.color.darker_gray)
        )
        DrawableCompat.setTint(
            DrawableCompat.wrap(ttsButton.drawable),
            ContextCompat.getColor(applicationContext, android.R.color.white)
        )
        Log.v(TRIG_TAG, poi.toString())

        // User-feedback for recommendation
        catViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        feedbackLikeButton.setOnClickListener {
            catViewModel.like(poi.categoryID)
            feedbackLikeButton.isEnabled = false
            feedbackDislikeButton.isEnabled = false
        }

        feedbackDislikeButton.setOnClickListener {
            catViewModel.dislike(poi.categoryID)
            feedbackLikeButton.isEnabled = false
            feedbackDislikeButton.isEnabled = false
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

    private fun toggleFavoritePOI(poi: POI) {
        MainScope().launch {
            poiSavedViewModel.toggleFavorite(poi, applicationContext, favorite_button)
        }
    }
}
