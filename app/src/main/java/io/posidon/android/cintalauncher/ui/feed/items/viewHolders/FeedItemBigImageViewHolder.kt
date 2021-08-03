package io.posidon.android.cintalauncher.ui.feed.items.viewHolders

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.view.updateLayoutParams
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

class FeedItemBigImageViewHolder(itemView: View) : FeedItemViewHolder(itemView) {
    val image = itemView.findViewById<ImageView>(R.id.image)!!
    val card = itemView.findViewById<CardView>(R.id.card)!!
    val requestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .downsample(DownsampleStrategy.AT_MOST)
        .sizeMultiplier(0.6f)

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
            val imageView = target.view as ImageView
            imageView.updateLayoutParams {
                val w = imageView.measuredWidth
                this.height = (w * resource.intrinsicHeight / resource.intrinsicWidth).coerceAtMost(w * 3 / 2)
            }
            target.onResourceReady(resource, null)
            return true
        }
    }
}

fun bindFeedItemBigImageViewHolder(
    holder: FeedItemBigImageViewHolder,
    item: FeedItemWithBigImage,
    color: Int
) {
    bindFeedItemViewHolder(holder, item, color)
    holder.card.setCardBackgroundColor(ColorTheme.cardBG)
    Glide.with(holder.itemView.context)
        .load(item.image)
        .apply(holder.requestOptions)
        .listener(holder.imageRequestListener)
        .into(holder.image)
}