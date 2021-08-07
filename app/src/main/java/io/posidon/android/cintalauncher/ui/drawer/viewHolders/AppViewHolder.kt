package io.posidon.android.cintalauncher.ui.drawer.viewHolders

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.ViewTarget
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.feed.items.FeedItemWithBigImage
import io.posidon.android.cintalauncher.data.feed.items.formatForAppCard
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.providers.AppSuggestionsManager
import io.posidon.android.cintalauncher.providers.FeedSorter
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.drawer.AppDrawerAdapter
import io.posidon.android.cintalauncher.ui.drawer.AppDrawerAdapter.Companion.APP_ITEM
import io.posidon.android.cintalauncher.ui.popup.drawerItem.ItemLongPress
import io.posidon.android.cintalauncher.ui.view.SeeThoughView
import posidon.android.conveniencelib.toBitmap

class AppViewHolder(
    val card: CardView,
    val map: HashMap<LauncherItem, () -> Unit>
) : RecyclerView.ViewHolder(card) {
    val icon = itemView.findViewById<ImageView>(R.id.icon_image)!!
    val label = itemView.findViewById<TextView>(R.id.icon_text)!!

    val iconSmall = itemView.findViewById<ImageView>(R.id.icon_image_small)!!
    val notificationView = itemView.findViewById<TextView>(R.id.icon_notifications)!!
    val imageView = itemView.findViewById<ImageView>(R.id.background_image)!!

    val blurBG = itemView.findViewById<SeeThoughView>(R.id.blur_bg)!!

    val requestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .downsample(DownsampleStrategy.AT_MOST)

    val imageRequestListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ) = false

        override fun onResourceReady(
            resource: Drawable,
            model: Any?,
            target: Target<Drawable>,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            target as ViewTarget<*, *>

            val palette = Palette.from(resource.toBitmap()).generate()
            val backgroundColor = ColorTheme.tintAppDrawerItem((palette.vibrantSwatch ?: palette.dominantSwatch)?.rgb ?: ColorTheme.appDrawerItemBase)

            card.setCardBackgroundColor(backgroundColor)
            label.setTextColor(ColorTheme.titleColorForBG(itemView.context, backgroundColor))
            notificationView.setTextColor(ColorTheme.textColorForBG(itemView.context, backgroundColor))

            target.onResourceReady(resource, null)
            return true
        }
    }
}

class AppItem(val item: App) : AppDrawerAdapter.DrawerItem {
    override fun getItemViewType() = APP_ITEM
    override val label: String
        get() = item.label
}

fun bindAppViewHolder(
    holder: AppViewHolder,
    item: LauncherItem,
    isDimmed: Boolean,
    suggestionsManager: AppSuggestionsManager,
    navbarHeight: Int,
) {
    holder.blurBG.drawable = BitmapDrawable(holder.itemView.resources, acrylicBlur?.smoothBlur)
    holder.map[item] = holder.blurBG::invalidate

    val backgroundColor = ColorTheme.tintAppDrawerItem(item.getColor())
    holder.card.setCardBackgroundColor(backgroundColor)
    holder.card.alpha = if (isDimmed) .3f else 1f
    holder.label.text = item.label
    holder.label.setTextColor(ColorTheme.titleColorForBG(holder.itemView.context, backgroundColor))
    holder.notificationView.setTextColor(ColorTheme.textColorForBG(holder.itemView.context, backgroundColor))

    val notifications = (item as? App)?.getNotifications()
    val notification = notifications?.let(FeedSorter::getMostRelevant)
    if (notification == null) {
        holder.iconSmall.isVisible = false
        holder.notificationView.isVisible = false
        holder.imageView.isVisible = false
        holder.icon.isVisible = true
        holder.icon.setImageDrawable(item.icon)
    } else {
        holder.iconSmall.isVisible = true
        holder.notificationView.isVisible = true
        holder.icon.isVisible = false
        holder.iconSmall.setImageDrawable(item.icon)
        holder.notificationView.text = notification.formatForAppCard(item)
        val image = (notification as? FeedItemWithBigImage)?.image
        if (image == null) holder.imageView.isVisible = false
        else {
            holder.imageView.isVisible = true
            holder.imageView.setImageDrawable(null)
            Glide.with(holder.itemView.context)
                .load(image)
                .apply(holder.requestOptions)
                .listener(holder.imageRequestListener)
                .into(holder.imageView)
        }
    }

    holder.itemView.setOnClickListener {
        suggestionsManager.onItemOpened(item)
        item.open(it.context.applicationContext, it)
    }
    holder.itemView.setOnLongClickListener {
        ItemLongPress.onItemLongPress(it.context, backgroundColor, ColorTheme.titleColorForBG(holder.itemView.context, backgroundColor), it, item, navbarHeight)
        true
    }
}