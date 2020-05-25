package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class ListGroupItems (

	@SerializedName("id") val id : String,
	@SerializedName("name") val name : String,
	@SerializedName("description") val description : String,
	@SerializedName("type") val type : String,
	@SerializedName("user") val user : User,
	@SerializedName("editable") val editable : Boolean,
	@SerializedName("public") val public : Boolean,
	@SerializedName("collaborative") val collaborative : Boolean,
	@SerializedName("url") val url : String,
	@SerializedName("canonicalUrl") val canonicalUrl : String,
	@SerializedName("createdAt") val createdAt : Int,
	@SerializedName("updatedAt") val updatedAt : Int,
	@SerializedName("photo") val photo : Photo,
	@SerializedName("logView") val logView : Boolean,
	@SerializedName("followers") val followers : Followers,
	@SerializedName("listItems") val listItems : ListItems
)