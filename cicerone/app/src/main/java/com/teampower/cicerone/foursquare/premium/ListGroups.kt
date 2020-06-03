package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class ListGroups (

	@SerializedName("type") val type : String,
	@SerializedName("name") val name : String,
	@SerializedName("count") val count : Int,
	@SerializedName("items") val items : List<ListGroupItems>
)