package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Contact (
	@SerializedName("phone") val phone : String,
	@SerializedName("formattedPhone") val formattedPhone : String,
	@SerializedName("twitter") val twitter : String,
	@SerializedName("instagram") val instagram : String,
	@SerializedName("facebook") val facebook : String,
	@SerializedName("facebookUsername") val facebookUsername : String,
	@SerializedName("facebookName") val facebookName : String
)