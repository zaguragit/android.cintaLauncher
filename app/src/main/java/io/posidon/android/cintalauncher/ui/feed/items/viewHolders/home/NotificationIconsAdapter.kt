package io.posidon.android.cintalauncher.ui.feed.items.viewHolders.home

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.providers.color.theme.ColorTheme

class NotificationIconsAdapter : RecyclerView.Adapter<NotificationIconsAdapter.IconViewHolder>() {

    private var items = emptyList<Drawable>()

    class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        return IconViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.feed_home_notification_icon, parent, false))
    }

    override fun onBindViewHolder(holder: IconViewHolder, i: Int) {
        holder.image.setImageDrawable(items[i])
        holder.image.imageTintList = ColorStateList.valueOf(ColorTheme.uiTitle)
    }

    override fun getItemCount() = items.size

    fun updateItems(items: List<Drawable>): Boolean {
        val numberChanged = this.items.size != items.size
        this.items = items
        notifyDataSetChanged()
        return numberChanged
    }
}