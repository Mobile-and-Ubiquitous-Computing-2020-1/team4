package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Entities (

	@SerializedName("indices") val indices : List<Int>,
	@SerializedName("type") val type : String
)