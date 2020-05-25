package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Page (

	@SerializedName("pageInfo") val pageInfo : PageInfo,
	@SerializedName("user") val user : User
)