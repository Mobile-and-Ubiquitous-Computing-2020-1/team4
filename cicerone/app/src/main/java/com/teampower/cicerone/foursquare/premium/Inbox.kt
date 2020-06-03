package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Inbox (

	@SerializedName("count") val count : Int,
	@SerializedName("items") val items : List<String>
)