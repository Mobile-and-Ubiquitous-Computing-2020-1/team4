package com.teampower.cicerone

import android.content.Context
import android.content.Intent
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
import com.teampower.cicerone.viewmodels.CategoryViewModel
import com.teampower.cicerone.viewmodels.POISavedViewModel
import kotlinx.android.synthetic.main.activity_geofence_triggered.*
import kotlinx.android.synthetic.main.activity_scrolling.toolbar
import kotlinx.android.synthetic.main.content_geofence_triggered.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

const val POI_DETAILS = "POI_DETAILS"


class GeofenceTriggeredActivity : AppCompatActivity() {
    private val TRIG_TAG = "POIActivity"
    private lateinit var catViewModel: CategoryViewModel
    private lateinit var poiViewModel: POISavedViewModel

    lateinit var tts: TextToSpeech
    private var speaking = false
    private var tts_text = ""
    private var isSaved = false

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

        poiViewModel = ViewModelProvider(this).get(POISavedViewModel::class.java)
        // Extract the transitionDetails
        val poiObjectJSON = intent.getStringExtra(POI_DETAILS) ?: ""
        val poi = MainActivity.fromJson<POI>(poiObjectJSON)
        MainScope().launch {
            val result = poiViewModel.loadPOI(poi.id).await()
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
        title = poi.name
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
            location_description.text = this.getString(
                R.string.location_description_yes_wikipedia,
                poi.wikipediaInfo?.extract
            )
            wikipedia_link_url.isClickable = true
            wikipedia_link_url.movementMethod = LinkMovementMethod.getInstance()
            val text =
                "<a href='${url_list["mobile"]?.get("page")}'> Link to Wikipedia article </a>"
            wikipedia_link_url.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
            tts_text = this.getString(
                R.string.tts_text_yes_wikipedia,
                poi.name,
                poi.category,
                poi.distance,
                poi.wikipediaInfo?.extract
            )
        } else {
            location_description.text = this.getString(
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
        tts_button.setOnClickListener {
            if (speaking) {
                tts.stop()
                speaking = false
                tts_button.setImageResource(R.drawable.ic_play_arrow_black_32dp)
                DrawableCompat.setTint(
                    DrawableCompat.wrap(tts_button.drawable),
                    ContextCompat.getColor(applicationContext, android.R.color.white)
                )
            } else {
                tts.speak(tts_text, TextToSpeech.QUEUE_FLUSH, null, "TRIG_TTS")
                tts_button.setImageResource(R.drawable.ic_stop_black_32dp)
                DrawableCompat.setTint(
                    DrawableCompat.wrap(tts_button.drawable),
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
            DrawableCompat.wrap(tts_button.drawable),
            ContextCompat.getColor(applicationContext, android.R.color.white)
        )
        Log.v(TRIG_TAG, poi.toString())

        // Demo on how to update category score
        catViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        catViewModel.updateCategoryPoints("School", 2.0) // Change score
    }

    private fun toggleFavoritePOI(poi: POI) {
        MainScope().launch {
            poiViewModel.toggleFavorite(poi, applicationContext, favorite_button)
        }
    }
}
