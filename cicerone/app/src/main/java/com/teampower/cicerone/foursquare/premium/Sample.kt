package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Sample (

	@SerializedName("entities") val entities : List<Entities>,
	@SerializedName("text") val text : String
)