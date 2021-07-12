package io.posidon.android.cintalauncher.ui.feed.items.viewHolders

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.feed.items.FeedItemWithBigImage
import io.posidon.android.cintalauncher.ui.color.ColorTheme

class FeedItemBigImageViewHolder(itemView: View) : FeedItemViewHolder(itemView) {
    val image = itemView.findViewById<ImageView>(R.id.image)
    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
}

fun bindFeedItemBigImageViewHolder(
    holder: FeedItemBigImageViewHolder,
    item: FeedItemWithBigImage,
    color: Int
) {
    bindFeedItemViewHolder(holder, item, color)
    Glide.with(holder.itemView.context)
        .load(item.image)
        .apply(holder.requestOptions)
        .addListener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
            override fun onResourceReady(
                resource: Drawable,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                holder.image.updateLayoutParams {
                    height = holder.image.measuredWidth * resource.intrinsicHeight / resource.intrinsicWidth
                }
                target!!.onResourceReady(resource, null)
                return true
            }
        })
        .into(holder.image)
}