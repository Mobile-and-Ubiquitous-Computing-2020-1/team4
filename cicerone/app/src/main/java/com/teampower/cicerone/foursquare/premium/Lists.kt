package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Lists (

	@SerializedName("groups") val groups : List<ListGroups>
)