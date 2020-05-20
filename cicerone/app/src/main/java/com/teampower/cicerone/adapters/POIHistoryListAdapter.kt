package com.teampower.cicerone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.teampower.cicerone.R
import com.teampower.cicerone.database.POIHistoryData

class POIHistoryListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<POIHistoryListAdapter.POIViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var pois = emptyList<POIHistoryData>() // Cached copy of words

    inner class POIViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poiItemView: TextView = itemView.findViewById(R.id.wordTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): POIViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return POIViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: POIViewHolder, position: Int) {
        val current = pois[position]
        holder.poiItemView.text = "ID: ${current.foursquareID} - ${current.name}, Type: ${current.category}"  // For some reasons template strings doesn't work here
    }

    internal fun setPOIs(pois: List<POIHistoryData>) {
        this.pois = pois
        notifyDataSetChanged()
    }

    override fun getItemCount() = pois.size
}