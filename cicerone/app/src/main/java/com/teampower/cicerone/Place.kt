package com.teampower.cicerone

data class Place(val name: String, val latitude: Double, val longitude: Double, val distance: Int, val address: String, val category: String, val description: String = "No description yet.") {
}