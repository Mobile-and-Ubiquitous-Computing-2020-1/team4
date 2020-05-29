package com.teampower.cicerone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.teampower.cicerone.R
import com.teampower.cicerone.database.POIData
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter

class POIListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<POIListAdapter.POIViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var pois = emptyList<POIData>() // Cached copy of words

    inner class POIViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var placeName: TextView = itemView.findViewById(R.id.listItemPlaceName)
        var subInfo: TextView = itemView.findViewById(R.id.listItemSubInfo)
        var shortText: TextView = itemView.findViewById(R.id.listItemShortText)
        var infoLayout: ConstraintLayout = itemView.findViewById(R.id.listItemInfoLayout)
        var starLayout: ConstraintLayout = itemView.findViewById(R.id.listItemStarLayout)
        var starImage: ImageView = itemView.findViewById(R.id.listItemStarImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): POIViewHolder {
        val itemView = inflater.inflate(R.layout.item_poi, parent, false)
        return POIViewHolder(itemView)
    }

    // Replace contents of view invoked by the layout manager
    override fun onBindViewHolder(holder: POIViewHolder, position: Int) {
        val currentItem = pois[position]
        holder.placeName.text = currentItem.name
        val timeFormatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC)
        val prettyTime = LocalDateTime.parse(currentItem.timeTriggered, timeFormatter).format(
            DateTimeFormatter.ofPattern("eee, MMM dd HH:mm")
        )
        holder.subInfo.text = holder.subInfo.context.getString(
            R.string.list_item_sub_info,
            prettyTime,
            currentItem.category
        )
        holder.shortText.text = holder.shortText.context.getString(
            R.string.list_item_short_text,
            currentItem.description
        )
        // TODO: Add function to send intent to open detail screen //holder.listItemLayout
        // TODO: Add function to star/unstar to button //holder.listItemStar
        // TODO: Change color of star based on saved or not.
    }

    internal fun setPOIs(pois: List<POIData>) {
        this.pois = pois
        notifyDataSetChanged()
    }

    override fun getItemCount() = pois.size
}