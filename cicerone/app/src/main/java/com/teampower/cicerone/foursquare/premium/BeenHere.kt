package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class BeenHere (

	@SerializedName("count") val count : Int,
	@SerializedName("unconfirmedCount") val unconfirmedCount : Int,
	@SerializedName("marked") val marked : Boolean,
	@SerializedName("lastCheckinExpiredAt") val lastCheckinExpiredAt : Int
)