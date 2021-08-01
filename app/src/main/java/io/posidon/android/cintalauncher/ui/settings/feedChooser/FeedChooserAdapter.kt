package io.posidon.android.cintalauncher.ui.settings.feedChooser

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.storage.Settings

class FeedChooserAdapter(
    private val settings: Settings,
    private val feedUrls: ArrayList<String>
) : RecyclerView.Adapter<FeedChooserAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var card: LinearLayout = itemView.findViewById(R.id.card)
        var text: TextView = itemView.findViewById(R.id.txt)
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val url = feedUrls[i]
        holder.text.text = url

        holder.card.backgroundTintList = ColorStateList.valueOf(ColorTheme.cardBG)
        holder.text.setTextColor(ColorTheme.cardDescription)
        holder.text.setOnLongClickListener {
            FeedSourcesChooserActivity.sourceEditPopup(it.context, settings, feedUrls, this, i)
            true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.feed_chooser_option, parent, false))
    }

    override fun getItemCount() = feedUrls.size

    override fun getItemId(i: Int) = 0L
}
