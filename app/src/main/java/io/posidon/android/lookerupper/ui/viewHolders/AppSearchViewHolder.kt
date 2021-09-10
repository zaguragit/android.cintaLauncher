package io.posidon.android.lookerupper.ui.viewHolders

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.providers.suggestions.SuggestionsManager
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.popup.drawerItem.ItemLongPress
import io.posidon.android.cintalauncher.ui.view.SeeThoughView
import io.posidon.android.lookerupper.data.results.AppResult
import io.posidon.android.lookerupper.data.results.SearchResult
import posidon.android.conveniencelib.getNavigationBarHeight

class AppSearchViewHolder(
    itemView: View,
    val activity: Activity,
    val map: HashMap<SearchResult, () -> Unit>
) : SearchViewHolder(itemView) {

    val icon = itemView.findViewById<ImageView>(R.id.icon_image)!!
    val label = itemView.findViewById<TextView>(R.id.icon_text)!!
    val card = itemView as CardView
    val blurBG = itemView.findViewById<SeeThoughView>(R.id.blur_bg)!!

    override fun onBind(result: SearchResult) {
        result as AppResult

        blurBG.drawable = BitmapDrawable(itemView.resources, acrylicBlur?.smoothBlur)
        map[result] = blurBG::invalidate

        val backgroundColor = ColorTheme.tintAppDrawerItem(result.getColor())
        card.setCardBackgroundColor(backgroundColor)
        label.text = result.title
        label.setTextColor(ColorTheme.titleColorForBG(itemView.context, backgroundColor))
        icon.setImageDrawable(result.icon)

        itemView.setOnClickListener {
            SuggestionsManager.onItemOpened(it.context, result.app)
            result.open(it)
        }
        itemView.setOnLongClickListener {
            ItemLongPress.onItemLongPress(
                it,
                backgroundColor,
                ColorTheme.titleColorForBG(itemView.context, backgroundColor),
                result.app,
                activity.getNavigationBarHeight(),
                onDragOut = { activity.finish() }
            )
            true
        }
    }
}