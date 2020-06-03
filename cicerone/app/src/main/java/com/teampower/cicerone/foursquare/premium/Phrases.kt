package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Phrases (

	@SerializedName("phrase") val phrase : String,
	@SerializedName("sample") val sample : Sample,
	@SerializedName("count") val count : Int
)