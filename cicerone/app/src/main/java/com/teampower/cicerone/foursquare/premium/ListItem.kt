package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class ListItem (

	@SerializedName("id") val id : String,
	@SerializedName("createdAt") val createdAt : Int,
	@SerializedName("photo") val photo : Photo
)