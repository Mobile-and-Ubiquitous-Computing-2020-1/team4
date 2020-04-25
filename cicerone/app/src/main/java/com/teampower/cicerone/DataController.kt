package com.teampower.cicerone

import android.content.Context
import android.widget.TextView
import android.util.Log
import android.util.Log.v as v1

class DataController {

    fun requestData(context: Context): Place {
        val description = "The United States Capitol, often called the Capitol Building, is the home of the United States Congress and the seat of the legislative branch of the U.S. federal government. It is located on Capitol Hill at the eastern end of the National Mall in Washington, D.C."
        var place = Place("United States Capitol", 38.8897, 77.0089, "First St SE, Washington, DC 20004, United States", description)
        return place
    }

    fun displayData(context: Context, place: Place, venue_description: TextView) {
        val place_string = StringBuilder()
        place_string.append("Name: ${place.name}").appendln()
        place_string.append("Location: ${place.longitude}, ${place.latitude}").appendln()
        place_string.append("Address: ${place.address}").appendln()
        place_string.appendln()
        place_string.append(place.description)

        venue_description.text = place_string
    }

}