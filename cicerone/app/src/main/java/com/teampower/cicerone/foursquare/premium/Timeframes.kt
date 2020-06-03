package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Timeframes (

	@SerializedName("days") val days : String,
	@SerializedName("open") val open : List<Open>,
	@SerializedName("segments") val segments : List<String>
)