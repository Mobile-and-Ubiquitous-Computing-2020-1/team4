package com.teampower.cicerone
import com.google.gson.annotations.SerializedName

data class Venues (
	@SerializedName("id") val id : String,
	@SerializedName("name") val name : String,
	@SerializedName("location") val location : Location,
	@SerializedName("categories") val categories : List<Categories>,
	@SerializedName("venuePage") val venuePage : VenuePage
)