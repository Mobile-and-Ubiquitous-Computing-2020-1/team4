package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Stats (

	@SerializedName("checkinsCount") val checkinsCount : Int,
	@SerializedName("usersCount") val usersCount : Int,
	@SerializedName("tipCount") val tipCount : Int,
	@SerializedName("visitsCount") val visitsCount : Int
)