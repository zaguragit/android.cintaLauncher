package io.posidon.android.lookerupper.ui.viewHolders

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
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
import io.posidon.android.cintalauncher.providers.FeedSorter
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.popup.drawerItem.ItemLongPress
import io.posidon.android.cintalauncher.ui.view.SeeThoughView
import io.posidon.android.lookerupper.data.results.AppResult
import io.posidon.android.lookerupper.data.results.SearchResult
import posidon.android.conveniencelib.toBitmap

class AppSearchViewHolder(
    itemView: View,
    val navbarHeight: Int,
    val activity: Activity,
    val map: HashMap<SearchResult, () -> Unit>
) : SearchViewHolder(itemView) {

    val icon = itemView.findViewById<ImageView>(R.id.icon_image)!!
    val label = itemView.findViewById<TextView>(R.id.icon_text)!!
    val card = itemView.findViewById<CardView>(R.id.card)!!

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

    override fun onBind(result: SearchResult) {
        result as AppResult

        blurBG.drawable = BitmapDrawable(itemView.resources, acrylicBlur?.smoothBlur)
        map[result] = blurBG::invalidate

        val backgroundColor = ColorTheme.tintAppDrawerItem(result.getColor())
        card.setCardBackgroundColor(backgroundColor)
        label.setTextColor(ColorTheme.titleColorForBG(itemView.context, backgroundColor))
        label.text = result.title
        label.setTextColor(ColorTheme.titleColorForBG(itemView.context, backgroundColor))
        notificationView.setTextColor(ColorTheme.textColorForBG(itemView.context, backgroundColor))

        val notifications = (result as? AppResult)?.getNotifications()
        val notification = notifications?.let(FeedSorter::getMostRelevant)
        if (notification == null) {
            iconSmall.isVisible = false
            notificationView.isVisible = false
            imageView.isVisible = false
            icon.isVisible = true
            icon.setImageDrawable(result.icon)
        } else {
            iconSmall.isVisible = true
            notificationView.isVisible = true
            icon.isVisible = false
            iconSmall.setImageDrawable(result.icon)
            notificationView.text = notification.formatForAppCard(result.app)
            val image = (notification as? FeedItemWithBigImage)?.image
            if (image == null) imageView.isVisible = false
            else {
                imageView.isVisible = true
                imageView.setImageDrawable(null)
                Glide.with(itemView.context)
                    .load(image)
                    .apply(requestOptions)
                    .listener(imageRequestListener)
                    .into(imageView)
            }
        }

        itemView.setOnClickListener(result::open)
        itemView.setOnLongClickListener {
            ItemLongPress.onItemLongPress(
                it,
                backgroundColor,
                ColorTheme.titleColorForBG(itemView.context, backgroundColor),
                result.app,
                navbarHeight,
                onDragOut = { activity.finish() }
            )
            true
        }
    }
}