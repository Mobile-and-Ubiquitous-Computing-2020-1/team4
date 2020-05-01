package com.teampower.cicerone
import com.google.gson.annotations.SerializedName

data class Response (

	@SerializedName("venues") val venues : List<Venues>
)