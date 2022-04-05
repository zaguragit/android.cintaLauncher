package io.posidon.android.cintalauncher.ui.feed

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.providers.color.theme.ColorTheme
import io.posidon.android.cintalauncher.data.feed.items.*
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.cintalauncher.ui.feed.items.viewHolders.*
import io.posidon.android.cintalauncher.ui.feed.items.viewHolders.home.HomeViewHolder
import io.posidon.android.cintalauncher.ui.feed.items.viewHolders.home.bindHomeViewHolder
import io.posidon.android.cintalauncher.ui.feed.items.viewHolders.suggestions.SuggestedViewHolder
import io.posidon.android.conveniencelib.drawable.toBitmap

class FeedAdapter(
    val activity: LauncherActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inline val context: Context get() = activity

    private var items: List<FeedItem> = emptyList()

    fun getFeedItem(i: Int) = items[i - 1]

    override fun getItemCount() = if (items.isEmpty()) 2 else items.size + 1

    override fun getItemId(i: Int) = if (i == 0) 0 else if (items.isEmpty()) -1 else getFeedItem(i).id

    override fun getItemViewType(i: Int) = when {
        i == 0 -> TYPE_HOME
        items.isEmpty() -> TYPE_EMPTY
        else -> when (getFeedItem(i)) {
            is FeedItemSuggestedApps -> TYPE_SUGGESTED
            is FeedItemWithMedia -> TYPE_MEDIA
            is FeedItemWithBigImage -> TYPE_BIG_IMAGE
            is FeedItemSmall -> TYPE_SMALL
            is FeedItemWithProgress -> TYPE_PROGRESS
            else -> TYPE_PLAIN
        }
    }

    private var homeViewHolder: HomeViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HOME -> HomeViewHolder(activity, activity.launcherContext, LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_home, parent, false)).also { homeViewHolder = it }
            TYPE_PLAIN -> FeedItemViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_plain, parent, false))
            TYPE_SMALL -> FeedItemSmallViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_small, parent, false))
            TYPE_BIG_IMAGE -> FeedItemImageViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_image, parent, false))
            TYPE_PROGRESS -> FeedItemProgressViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_progress, parent, false))
            TYPE_MEDIA -> FeedItemMediaViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_media, parent, false))
            TYPE_SUGGESTED -> SuggestedViewHolder(activity, LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_suggested_apps, parent, false))
            TYPE_EMPTY -> EmptyFeedItemViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_empty, parent, false))
            else -> throw RuntimeException("Invalid view holder type")
        }
    }

    private val themedColorCache = HashMap<Pair<Drawable?, Int>, Int>()
    private val colorCache = HashMap<Pair<Drawable?, Int>, Int>()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
        if (i == 0) {
            return bindHomeViewHolder(holder as HomeViewHolder)
        }
        if (holder.itemViewType == TYPE_EMPTY) {
            bindEmptyFeedItemViewHolder(holder as EmptyFeedItemViewHolder)
            return
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
            ColorTheme.adjustColorForContrast(ColorTheme.uiBG, color)
        }
        holder as FeedViewHolder
        holder.onBind(item, color)
        if (holder !is FeedItemViewHolder) return
        val verticalPadding = holder.itemView.resources.getDimension(R.dimen.feed_card_padding_vertical).toInt()
        holder.container.setPadding(
            holder.container.paddingLeft,
            holder.container.paddingTop,
            holder.container.paddingRight,
            if (i == itemCount - 1) verticalPadding + activity.getFeedBottomMargin() else verticalPadding,
        )
    }

    fun updateItems(items: List<FeedItem>) {
        val diffCallback = FeedDiffCallback(this.items, items)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.items = items
        diffResult.dispatchUpdatesTo(this)
    }

    fun onFeedInitialized() {
    }

    fun updateColorTheme() {
        themedColorCache.clear()
        notifyItemRangeChanged(0, itemCount)
    }

    companion object {
        private const val TYPE_HOME = 0
        private const val TYPE_PLAIN = 1
        private const val TYPE_SMALL = 2
        private const val TYPE_BIG_IMAGE = 3
        private const val TYPE_PROGRESS = 4
        private const val TYPE_MEDIA = 5
        private const val TYPE_SUGGESTED = 6
        private const val TYPE_EMPTY = 7
    }
}