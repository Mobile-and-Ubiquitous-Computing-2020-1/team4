package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class User (

	@SerializedName("id") val id : Int,
	@SerializedName("firstName") val firstName : String,
	@SerializedName("photo") val photo : Photo,
	@SerializedName("type") val type : String
)