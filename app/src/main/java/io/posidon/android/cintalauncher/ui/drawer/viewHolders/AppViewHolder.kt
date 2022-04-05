package io.posidon.android.cintalauncher.ui.drawer.viewHolders

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.providers.color.theme.ColorTheme
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.providers.feed.suggestions.SuggestionsManager
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.drawer.AppDrawer.Companion.WIDTH_TO_HEIGHT
import io.posidon.android.cintalauncher.ui.drawer.AppDrawerAdapter
import io.posidon.android.cintalauncher.ui.drawer.AppDrawerAdapter.Companion.APP_ITEM
import io.posidon.android.cintalauncher.ui.feed.items.viewHolders.applyIfNotNull
import io.posidon.android.cintalauncher.ui.popup.appItem.ItemLongPress
import io.posidon.android.cintalauncher.ui.view.HorizontalAspectRatioLayout
import io.posidon.android.cintalauncher.ui.view.SeeThoughView
import io.posidon.android.conveniencelib.getNavigationBarHeight
import io.posidon.android.conveniencelib.drawable.toBitmap

class AppViewHolder(
    val card: CardView
) : RecyclerView.ViewHolder(card) {
    val icon = itemView.findViewById<ImageView>(R.id.icon_image)!!
    val label = itemView.findViewById<TextView>(R.id.icon_text)!!

    val iconSmall = itemView.findViewById<ImageView>(R.id.icon_image_small)!!

    val spacer = itemView.findViewById<View>(R.id.spacer)!!

    val lineTitle = itemView.findViewById<TextView>(R.id.line_title)!!
    val lineDescription = itemView.findViewById<TextView>(R.id.line_description)!!

    val imageView = itemView.findViewById<ImageView>(R.id.background_image)!!

    val blurBG = itemView.findViewById<SeeThoughView>(R.id.blur_bg)!!

    val aspect = itemView.findViewById<HorizontalAspectRatioLayout>(R.id.aspect)!!.apply {
        widthToHeight = WIDTH_TO_HEIGHT
    }

    val requestOptions = RequestOptions()
        .downsample(DownsampleStrategy.AT_MOST)

    class ImageRequestListener(
        val holder: AppViewHolder,
        val color: Int,
    ) : RequestListener<Drawable> {
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
            val palette = Palette.from(resource.toBitmap(32, 32)).generate()
            val backgroundColor = ColorTheme.tintWithColor(ColorTheme.cardBG, palette.getDominantColor(color))
            val actuallyBackgroundColor = ColorUtils.blendARGB(backgroundColor, color, holder.imageView.alpha)

            holder.card.setCardBackgroundColor(backgroundColor)
            holder.label.setTextColor(ColorTheme.titleColorForBG(actuallyBackgroundColor))
            holder.lineTitle.setTextColor(ColorTheme.titleColorForBG(actuallyBackgroundColor))
            holder.lineDescription.setTextColor(ColorTheme.textColorForBG(actuallyBackgroundColor))

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
    activity: Activity,
) {
    holder.blurBG.drawable = BitmapDrawable(holder.itemView.resources, acrylicBlur?.insaneBlur)

    val backgroundColor = ColorTheme.tintWithColor(ColorTheme.cardBG, item.getColor())
    holder.card.setCardBackgroundColor(backgroundColor)
    holder.card.alpha = if (isDimmed) .3f else 1f
    holder.label.text = item.label
    holder.label.setTextColor(ColorTheme.titleColorForBG(backgroundColor))
    holder.lineTitle.setTextColor(ColorTheme.titleColorForBG(backgroundColor))
    holder.lineDescription.setTextColor(ColorTheme.textColorForBG(backgroundColor))

    val banner = (item as? App)?.getBanner()
    if (banner?.text == null && banner?.title == null) {
        holder.iconSmall.isVisible = false
        holder.spacer.isVisible = true
        holder.icon.isVisible = true
        holder.icon.setImageDrawable(item.icon)
    } else {
        holder.iconSmall.isVisible = true
        holder.spacer.isVisible = false
        holder.icon.isVisible = false
        holder.iconSmall.setImageDrawable(item.icon)
    }
    applyIfNotNull(holder.lineTitle, banner?.title, TextView::setText)
    applyIfNotNull(holder.lineDescription, banner?.text, TextView::setText)
    if (banner?.background == null) {
        holder.imageView.isVisible = false
    } else {
        holder.imageView.isVisible = true
        holder.imageView.setImageDrawable(null)
        holder.imageView.alpha = banner.bgOpacity
        Glide.with(holder.itemView.context)
            .load(banner.background)
            .apply(holder.requestOptions)
            .listener(AppViewHolder.ImageRequestListener(holder, item.getColor()))
            .into(holder.imageView)
    }

    holder.itemView.setOnClickListener {
        SuggestionsManager.onItemOpened(it.context, item)
        item.open(it.context.applicationContext, it)
    }
    holder.itemView.setOnLongClickListener {
        ItemLongPress.onItemLongPress(
            it,
            backgroundColor,
            ColorTheme.titleColorForBG(backgroundColor),
            item,
            activity.getNavigationBarHeight(),
        )
        true
    }
}