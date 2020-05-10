package com.teampower.cicerone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_geofence_triggered.*
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.android.synthetic.main.activity_scrolling.toolbar
import kotlinx.android.synthetic.main.content_geofence_triggered.*

class GeofenceTriggeredActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofence_triggered)
        setSupportActionBar(toolbar)

        // Extract the transitionDetails
        val transitionDetails = intent.getStringExtra("TRANSITION_DETAILS")

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Home button to MainActivity

        // Update the view
        setTitle(transitionDetails)
        geofence_information.text = "You crossed the Geofence with ID:${transitionDetails} - Cool dude"

    }
}
