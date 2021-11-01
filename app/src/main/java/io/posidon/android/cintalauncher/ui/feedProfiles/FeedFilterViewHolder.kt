package io.posidon.android.cintalauncher.ui.feedProfiles

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R

class FeedFilterViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    val text = itemView.findViewById<TextView>(R.id.text)
    val icon = itemView.findViewById<ImageView>(R.id.icon)
    val card = itemView.findViewById<CardView>(R.id.card)
}