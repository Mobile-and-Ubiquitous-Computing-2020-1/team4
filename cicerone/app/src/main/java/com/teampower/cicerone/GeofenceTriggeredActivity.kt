package com.teampower.cicerone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_scrolling.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_geofence_triggered.*
import kotlinx.android.synthetic.main.activity_scrolling.toolbar
import kotlinx.android.synthetic.main.content_geofence_triggered.*


class GeofenceTriggeredActivity : AppCompatActivity()  {
    private val TRIG_TAG = "Location"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofence_triggered)
        setSupportActionBar(toolbar)

        // Extract the transitionDetails
        val placeDetailsJson = intent.getStringExtra("PLACE_DETAILS") ?: ""
        val placeDetails = MainActivity.fromJson<POI.PlaceDetails>(placeDetailsJson)
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

        Log.v(TRIG_TAG, POI.toString())
    }

}
