package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class TipItems (

	@SerializedName("id") val id : String,
	@SerializedName("createdAt") val createdAt : Int,
	@SerializedName("text") val text : String,
	@SerializedName("type") val type : String,
	@SerializedName("canonicalUrl") val canonicalUrl : String,
	@SerializedName("photo") val photo : Photo,
	@SerializedName("photourl") val photourl : String,
	@SerializedName("likes") val likes : Likes,
	@SerializedName("logView") val logView : Boolean,
	@SerializedName("agreeCount") val agreeCount : Int,
	@SerializedName("disagreeCount") val disagreeCount : Int,
	@SerializedName("todo") val todo : Todo,
	@SerializedName("user") val user : User,
	@SerializedName("authorInteractionType") val authorInteractionType : String
)