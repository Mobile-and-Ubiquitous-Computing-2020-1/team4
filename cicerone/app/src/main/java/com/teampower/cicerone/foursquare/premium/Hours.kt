package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Hours (

	@SerializedName("status") val status : String,
	@SerializedName("isOpen") val isOpen : Boolean,
	@SerializedName("isLocalHoliday") val isLocalHoliday : Boolean,
	@SerializedName("timeframes") val timeframes : List<Timeframes>
)