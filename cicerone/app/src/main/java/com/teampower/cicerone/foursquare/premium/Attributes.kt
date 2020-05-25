package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Attributes (
	@SerializedName("groups") val groups : List<AttributeGroups>
)