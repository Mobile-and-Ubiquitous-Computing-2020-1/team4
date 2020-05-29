package com.teampower.cicerone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.teampower.cicerone.MainActivity
import com.teampower.cicerone.R
import com.teampower.cicerone.database.POIData
import com.teampower.cicerone.wikipedia.WikipediaPlaceInfo
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

class POIListAdapter internal constructor(
    private val context: Context
) : RecyclerView.Adapter<POIListAdapter.POIViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var pois: List<POIData> = emptyList() // Cached copy of words

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
        val timeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
        val prettyTime = ZonedDateTime.parse(currentItem.timeTriggered, timeFormatter).format(
            DateTimeFormatter.ofPattern("eee, MMM dd HH:mm")
        )
        holder.subInfo.text = holder.subInfo.context.getString(
            R.string.list_item_sub_info,
            prettyTime,
            currentItem.category
        )
        val wikiInfo: WikipediaPlaceInfo? =
            currentItem.wikipediaInfoJSON?.let { MainActivity.fromJson<WikipediaPlaceInfo>(it) }
        val shortText =
            if (currentItem.description != "") currentItem.description else wikiInfo?.description
        holder.shortText.text = holder.shortText.context.getString(
            R.string.list_item_short_text,
            shortText
        )
        if (shortText == null) holder.shortText.visibility = View.INVISIBLE
        // TODO: Add function to send intent to open detail screen //holder.listItemLayout
        // TODO: Add function to star/unstar to button //holder.listItemStar
        // TODO: Change color of star based on saved or not.

        holder.starLayout.setOnClickListener {
            DrawableCompat.setTint(
                DrawableCompat.wrap(holder.starImage.drawable),
                ContextCompat.getColor(context, R.color.yellow)
            )
        }
    }

    internal fun setPOIs(pois: List<POIData>) {
        this.pois = pois
        notifyDataSetChanged()
    }

    override fun getItemCount() = pois.size
}