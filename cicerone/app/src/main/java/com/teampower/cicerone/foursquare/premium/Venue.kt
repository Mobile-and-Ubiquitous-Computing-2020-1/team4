package com.teampower.cicerone.foursquare.premium
import com.google.gson.annotations.SerializedName

data class Venue (

	@SerializedName("id") val id : String,
	@SerializedName("name") val name : String,
	@SerializedName("contact") val contact : Contact,
	@SerializedName("location") val location : Location,
	@SerializedName("canonicalUrl") val canonicalUrl : String,
	@SerializedName("categories") val categories : List<Categories>,
	@SerializedName("verified") val verified : Boolean,
	@SerializedName("stats") val stats : Stats,
	@SerializedName("url") val url : String,
	@SerializedName("likes") val likes : Likes,
	@SerializedName("rating") val rating : Double,
	@SerializedName("ratingColor") val ratingColor : String,
	@SerializedName("ratingSignals") val ratingSignals : Int,
	@SerializedName("beenHere") val beenHere : BeenHere,
	@SerializedName("photos") val photos : Photos,
	@SerializedName("description") val description : String,
	@SerializedName("storeId") val storeId : String,
	@SerializedName("page") val page : Page,
	@SerializedName("hereNow") val hereNow : HereNow,
	@SerializedName("createdAt") val createdAt : Int,
	@SerializedName("tips") val tips : Tips,
	@SerializedName("shortUrl") val shortUrl : String,
	@SerializedName("timeZone") val timeZone : String,
	@SerializedName("listed") val listed : Listed,
	@SerializedName("phrases") val phrases : List<Phrases>,
	@SerializedName("hours") val hours : Hours,
	@SerializedName("popular") val popular : Popular,
	@SerializedName("pageUpdates") val pageUpdates : PageUpdates,
	@SerializedName("inbox") val inbox : Inbox,
	@SerializedName("venueChains") val venueChains : List<String>,
	@SerializedName("attributes") val attributes : Attributes,
	@SerializedName("bestPhoto") val bestPhoto : BestPhoto
)