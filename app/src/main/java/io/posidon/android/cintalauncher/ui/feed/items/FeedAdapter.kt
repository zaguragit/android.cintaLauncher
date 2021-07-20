package io.posidon.android.cintalauncher.ui.feed.items

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.data.feed.items.FeedItemSmall
import io.posidon.android.cintalauncher.data.feed.items.FeedItemWithBigImage
import io.posidon.android.cintalauncher.data.feed.items.FeedItemWithProgress
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.cintalauncher.ui.feed.home.HomeViewHolder
import io.posidon.android.cintalauncher.ui.feed.home.bindHomeViewHolder
import io.posidon.android.cintalauncher.ui.feed.items.viewHolders.*
import posidon.android.conveniencelib.Graphics
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.toBitmap

class FeedAdapter(
    val activity: LauncherActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inline val context: Context get() = activity

    private var itemList: List<FeedItem> = emptyList()

    fun getFeedItem(i: Int) = itemList[i - 1]

    override fun getItemCount() = itemList.size + 1

    override fun getItemId(i: Int) = if (i == 0) 0 else getFeedItem(i).id

    override fun getItemViewType(i: Int) = if (i == 0) TYPE_HOME else when (getFeedItem(i)) {
        is FeedItemWithBigImage -> TYPE_BIG_IMAGE
        is FeedItemSmall -> TYPE_SMALL
        is FeedItemWithProgress -> TYPE_PROGRESS
        else -> TYPE_PLAIN
    }

    private var homeViewHolder: HomeViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HOME -> HomeViewHolder(scrollIndicator.also { homeViewHolder?.vertical?.removeView(it) }, parent, activity, LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_home, parent, false)).also { homeViewHolder = it }
            TYPE_PLAIN -> FeedItemViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_plain, parent, false))
            TYPE_SMALL -> FeedItemSmallViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_small, parent, false))
            TYPE_BIG_IMAGE -> FeedItemBigImageViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_big_image, parent, false))
            TYPE_PROGRESS -> FeedItemViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_plain, parent, false))
            else -> throw RuntimeException("Invalid view holder type")
        }
    }

    private val themedColorCache = HashMap<Pair<Drawable?, Int>, Int>()
    private val colorCache = HashMap<Pair<Drawable?, Int>, Int>()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
        if (i == 0) {
            return bindHomeViewHolder(holder as HomeViewHolder)
        }
        val item = getFeedItem(i)
        val k = item.sourceIcon to item.color
        val color = themedColorCache.getOrPut(k) {
            val color = colorCache.getOrPut(k) {
                if (item.shouldTintIcon || item.sourceIcon == null) {
                    if (item.color == 0) ColorTheme.accentColor else item.color
                } else if (item.color != 0) item.color else {
                    val accent = ColorTheme.accentColor
                    Palette.from(item.sourceIcon!!.toBitmap()).generate().getDominantColor(accent)
                }
            }
            ColorTheme.forCardBackground(color)
        }
        when (holder.itemViewType) {
            TYPE_PLAIN -> bindFeedItemViewHolder(holder as FeedItemViewHolder, item, color)
            TYPE_SMALL -> bindFeedItemSmallViewHolder(holder as FeedItemSmallViewHolder, item as FeedItemSmall, color)
            TYPE_BIG_IMAGE -> bindFeedItemBigImageViewHolder(holder as FeedItemBigImageViewHolder, item as FeedItemWithBigImage, color)
            TYPE_PROGRESS -> bindFeedItemViewHolder(holder as FeedItemViewHolder, item as FeedItemWithProgress, color)
        }
        holder as FeedItemViewHolder
        if (i == 1) {
            (holder.card.layoutParams as ViewGroup.MarginLayoutParams).topMargin = context.dp(8).toInt()
        } else {
            (holder.card.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 0
        }
    }

    fun updateItems(items: List<FeedItem>) {
        this.itemList = items
        notifyDataSetChanged()
    }

    private val scrollIndicatorDrawable = ContextCompat.getDrawable(context, R.drawable.loading)!!
    private val scrollIndicator = ImageView(context).apply {
        setImageDrawable(Graphics.tryAnimate(activity, scrollIndicatorDrawable))
        imageTintList = ColorStateList.valueOf(0xffffffff.toInt())
    }
    fun onFeedInitialized() {
        Graphics.clearAnimation(scrollIndicatorDrawable)
        scrollIndicator.setImageResource(R.drawable.ic_swipe_up)
    }

    fun updateColorTheme() {
        themedColorCache.clear()
        notifyDataSetChanged()
    }

    fun onAppsLoaded() {
        homeViewHolder?.updateRecents()
    }

    companion object {
        private const val TYPE_HOME = 0
        private const val TYPE_PLAIN = 1
        private const val TYPE_SMALL = 2
        private const val TYPE_BIG_IMAGE = 3
        private const val TYPE_PROGRESS = 4
    }
}