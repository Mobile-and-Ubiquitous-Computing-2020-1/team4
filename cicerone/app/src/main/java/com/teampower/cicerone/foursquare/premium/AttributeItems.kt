package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class AttributeItems (
	@SerializedName("displayName") val displayName : String,
	@SerializedName("displayValue") val displayValue : String
)