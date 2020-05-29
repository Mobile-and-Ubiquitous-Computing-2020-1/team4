package com.teampower.cicerone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.teampower.cicerone.MainActivity
import com.teampower.cicerone.R
import com.teampower.cicerone.database.POIData
import com.teampower.cicerone.wikipedia.WikipediaPlaceInfo
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

class POIListAdapter internal constructor(
    private val context: Context,
    val onItemClick: ((POIData) -> Unit),
    val onStarClicked: ((POIData, POIListAdapter.POIViewHolder) -> Unit)
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

        fun bind(poiItem: POIData) {
            placeName.text = poiItem.name
            val timeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
            val prettyTime = ZonedDateTime.parse(poiItem.timeTriggered, timeFormatter).format(
                DateTimeFormatter.ofPattern("eee, MMM dd HH:mm")
            )
            subInfo.text = subInfo.context.getString(
                R.string.list_item_sub_info,
                prettyTime,
                poiItem.category
            )
            val wikiInfo: WikipediaPlaceInfo? =
                poiItem.wikipediaInfoJSON?.let { MainActivity.fromJson<WikipediaPlaceInfo>(it) }
            val description =
                if (poiItem.description != "") poiItem.description else wikiInfo?.description
            shortText.text = shortText.context.getString(
                R.string.list_item_short_text,
                description
            )
            if (description == "") shortText.visibility = View.INVISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): POIViewHolder {
        val itemView = inflater.inflate(R.layout.item_poi, parent, false)
        val holder = POIViewHolder(itemView)
        // Add click listeners
        holder.infoLayout.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                onItemClick.invoke(pois[holder.adapterPosition])
            }
        }
        // TODO: Add function to star/unstar to button //listItemStar
        // TODO: Change color of star based on saved or not.
        holder.starLayout.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                onStarClicked.invoke(pois[holder.adapterPosition], holder)
            }
        }
        return holder
    }

    // Replace contents of view invoked by the layout manager
    override fun onBindViewHolder(holder: POIViewHolder, position: Int) {
        // Verify if position exists in list
        if (position != RecyclerView.NO_POSITION) {
            val poiItem: POIData = pois[position]
            holder.bind(poiItem)
        }
    }

    internal fun setPOIs(pois: List<POIData>) {
        this.pois = pois
        notifyDataSetChanged()
    }

    override fun getItemCount() = pois.size
}
