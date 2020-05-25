package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class AttributeGroups (

	@SerializedName("type") val type : String,
	@SerializedName("name") val name : String,
	@SerializedName("summary") val summary : String,
	@SerializedName("count") val count : Int,
	@SerializedName("items") val items : List<AttributeItems>
)