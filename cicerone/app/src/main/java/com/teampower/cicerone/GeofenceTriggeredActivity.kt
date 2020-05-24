package com.teampower.cicerone

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import com.google.gson.internal.LinkedTreeMap
import com.teampower.cicerone.database.POISavedData
import com.teampower.cicerone.database.history_table.POISavedViewModel
import com.teampower.cicerone.viewmodels.CategoryViewModel
import kotlinx.android.synthetic.main.activity_geofence_triggered.*
import kotlinx.android.synthetic.main.activity_scrolling.toolbar
import kotlinx.android.synthetic.main.content_geofence_triggered.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import java.util.*


class GeofenceTriggeredActivity : AppCompatActivity() {
    private val TRIG_TAG = "POIActivity"
    private lateinit var catViewModel: CategoryViewModel
    private lateinit var poiSavedViewModel: POISavedViewModel

    lateinit var tts: TextToSpeech
    private var speaking = false
    private var tts_text = ""
    private var isSaved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofence_triggered)
        setSupportActionBar(toolbar)

        poiSavedViewModel = ViewModelProvider(this).get(POISavedViewModel::class.java)
        // Extract the transitionDetails
        val placeDetailsJson = intent.getStringExtra("PLACE_DETAILS") ?: ""
        val placeDetails = MainActivity.fromJson<PlaceDetails>(placeDetailsJson)
        val POI = placeDetails.poi
        MainScope().launch {
            val result = poiSavedViewModel.loadPOI(POI.id).await()
            isSaved = result !== null
            if (isSaved) {
                DrawableCompat.setTint(
                    DrawableCompat.wrap(favorite_button.drawable),
                    ContextCompat.getColor(applicationContext, R.color.yellow)
                )
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Home button to MainActivity

        // Update the view
        title = POI.name
        location_category.text = this.getString(
            R.string.location_category,
            POI.category
        )
        location_distance.text = this.getString(
            R.string.location_distance,
            POI?.distance.toString()
        )
        if (placeDetails.wikipediaInfo != null) {
            // For some reason didn't work to set proper type in WikipediaResponseModule.kt
            val url_list: LinkedTreeMap<String, LinkedTreeMap<String, String>> =
                placeDetails.wikipediaInfo.content_urls as LinkedTreeMap<String, LinkedTreeMap<String, String>>
            Log.i(TRIG_TAG, "${url_list::class.simpleName}")
            Log.i(TRIG_TAG, "${url_list["mobile"]?.get("page")}")
            location_description.text = this.getString(
                R.string.location_description_yes_wikipedia,
                placeDetails.wikipediaInfo.extract
            )
            wikipedia_link_url.setClickable(true)
            wikipedia_link_url.setMovementMethod(LinkMovementMethod.getInstance())
            val text =
                "<a href='${url_list["mobile"]?.get("page")}'> Link to Wikipedia article </a>"
            wikipedia_link_url.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
            tts_text = this.getString(
                R.string.tts_text_yes_wikipedia,
                POI.name,
                POI.category,
                POI.distance,
                placeDetails.wikipediaInfo.extract
            )
        } else {
            location_description.text = this.getString(
                R.string.location_description_no_wikipedia
            )
            tts_text = this.getString(
                R.string.tts_text_no_wikipedia,
                POI.name,
                POI.category,
                POI.distance
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
        tts_button.setOnClickListener {
            if (speaking) {
                tts.stop()
                speaking = false
                tts_button.setImageResource(R.drawable.ic_play_arrow_black_32dp)
            } else {
                tts.speak(tts_text, TextToSpeech.QUEUE_FLUSH, null, "TRIG_TTS")
                tts_button.setImageResource(R.drawable.ic_stop_black_32dp)
            }
        }

        favorite_button.setOnClickListener { toggleFavoritePOI(POI) }
        Log.v(TRIG_TAG, POI.toString())

        // Demo on how to update category score
        catViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        catViewModel.updateCategoryPoints("School", 2.0) // Change score
    }

    private fun toggleFavoritePOI(poi: POI) {
        MainScope().launch {
            val result = poiSavedViewModel.loadPOI(poi.id).await()
            isSaved = result !== null

            if (!isSaved) {
                // Add to favorites
                val currentTimeString = Instant.now().toString()
                poiSavedViewModel.favorite(
                    POISavedData(
                        poi.id,
                        poi.name,
                        poi.category,
                        currentTimeString,
                        poi.lat,
                        poi.long
                    )
                )
                isSaved = true
                DrawableCompat.setTint(
                    DrawableCompat.wrap(favorite_button.drawable),
                    ContextCompat.getColor(applicationContext, R.color.yellow)
                )
                Log.i(TRIG_TAG, "Added POI to favorites")

            } else {
                // Remove from favorites
                poiSavedViewModel.unFavorite(poi.id)
                isSaved = false
                DrawableCompat.setTint(
                    DrawableCompat.wrap(favorite_button.drawable),
                    ContextCompat.getColor(applicationContext, R.color.black)
                )
                Log.i(TRIG_TAG, "Removed POI from favorites")
            }
        }
    }
}
