package com.teampower.cicerone
import com.google.gson.annotations.SerializedName

object FoursquareResponse {
    data class Result(val data: FoursquareData)
}
