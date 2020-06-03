package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class FoursquarePremiumData (
	@SerializedName("meta") val meta : Meta,
	@SerializedName("response") val response : Response
)