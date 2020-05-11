package com.teampower.cicerone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.android.synthetic.main.content_geofence_triggered.*

class GeofenceTriggeredActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofence_triggered)
        setSupportActionBar(toolbar)

        // Extract the transitionDetails
        val placeDetailsJson = intent.getStringExtra("PLACE_DETAILS") ?: ""
        val placeDetails = MainActivity.fromJson<PlaceDetails>(placeDetailsJson)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Home button to MainActivity

        // Update the view
        title = placeDetails.poi.name
        wikipediaInfo.text = placeDetails.wikipediaInfo?.extract

    }
}
