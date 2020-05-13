package com.teampower.cicerone

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_geofence_triggered.*
import kotlinx.android.synthetic.main.activity_scrolling.toolbar
import kotlinx.android.synthetic.main.content_geofence_triggered.*
import java.util.*


class GeofenceTriggeredActivity : AppCompatActivity()  {
    private val TRIG_TAG = "POIActivity"
    lateinit var tts : TextToSpeech
    private var speaking = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofence_triggered)
        setSupportActionBar(toolbar)

        // Extract the transitionDetails
        val placeDetailsJson = intent.getStringExtra("PLACE_DETAILS") ?: ""
        val placeDetails = MainActivity.fromJson<PlaceDetails>(placeDetailsJson)
        val POI = placeDetails.poi
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Home button to MainActivity

        // Update the view
        title = POI?.name
        location_category.text = this.getString(
            R.string.location_category,
            POI?.category
        )
        location_distance.text = this.getString(
            R.string.location_distance,
            POI?.distance.toString()
        )
        location_description.text = this.getString(
            R.string.location_description,
            placeDetails.wikipediaInfo?.extract
        )
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
            if(speaking){
                tts.stop()
                speaking = false
            } else{
                tts.speak(placeDetails.wikipediaInfo?.extract, TextToSpeech.QUEUE_FLUSH, null, "TRIG_TTS")
            }
        }
    }
}
