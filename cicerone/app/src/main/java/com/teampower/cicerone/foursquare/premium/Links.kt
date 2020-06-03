package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Links (

	@SerializedName("count") val count : Int,
	@SerializedName("items") val items : List<LinkItems>
)