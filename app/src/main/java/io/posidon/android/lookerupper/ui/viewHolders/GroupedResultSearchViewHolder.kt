package io.posidon.android.lookerupper.ui.viewHolders

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.view.View
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.view.SeeThoughView
import io.posidon.android.lookerupper.data.results.GroupedResult
import io.posidon.android.lookerupper.data.results.SearchResult
import io.posidon.android.lookerupper.ui.SearchAdapter

class GroupedResultSearchViewHolder(
    itemView: View,
    val activity: Activity,
    val map: HashMap<SearchResult, () -> Unit>,
    val parentRecyclerView: RecyclerView
) : SearchViewHolder(itemView) {

    val recyclerView = itemView.findViewById<RecyclerView>(R.id.recycler)!!
    val recyclerAdapter = SearchAdapter(activity, recyclerView, true)
    init {
        recyclerView.run {
            adapter = recyclerAdapter
            setRecycledViewPool(parentRecyclerView.recycledViewPool)
            layoutManager = LinearLayoutManager(itemView.context, RecyclerView.VERTICAL, false)
        }
    }
    val card = itemView.findViewById<CardView>(R.id.card)!!
    val blurBG = itemView.findViewById<SeeThoughView>(R.id.blur_bg)!!

    override fun onBind(result: SearchResult) {
        result as GroupedResult

        blurBG.drawable = BitmapDrawable(itemView.resources, acrylicBlur?.smoothBlur)
        map[result] = blurBG::invalidate

        card.setCardBackgroundColor(ColorTheme.cardBG)

        recyclerAdapter.update(result.children)
    }
}