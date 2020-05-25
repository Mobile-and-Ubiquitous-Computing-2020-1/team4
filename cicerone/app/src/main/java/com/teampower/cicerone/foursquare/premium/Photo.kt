package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Photo (

	@SerializedName("id") val id : String,
	@SerializedName("createdAt") val createdAt : Int,
	@SerializedName("prefix") val prefix : String,
	@SerializedName("suffix") val suffix : String,
	@SerializedName("width") val width : Int,
	@SerializedName("height") val height : Int,
	@SerializedName("visibility") val visibility : String
)