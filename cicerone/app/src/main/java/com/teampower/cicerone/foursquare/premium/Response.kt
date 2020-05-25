package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Response (

	@SerializedName("venue") val venue : Venue
)