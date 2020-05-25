package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Listed (

	@SerializedName("count") val count : Int,
	@SerializedName("groups") val groups : List<ListGroups>
)