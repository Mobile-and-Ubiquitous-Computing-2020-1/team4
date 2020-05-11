package com.teampower.cicerone

import android.os.Bundle
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
        val POI = intent?.getStringExtra("TRANSITION_DETAILS")?.let { MainActivity.fromJson<POI>(it) }

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
            POI?.description
        )

        Log.v(TRIG_TAG, POI.toString())
    }

}
