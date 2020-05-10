package com.teampower.cicerone
import com.google.gson.annotations.SerializedName

data class FoursquareData (
    @SerializedName("meta") val meta : Meta,
    @SerializedName("response") val response : Response
)