package io.posidon.android.lookerupper.ui.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.providers.color.theme.ColorTheme
import io.posidon.android.lookerupper.data.results.ContactResult
import io.posidon.android.lookerupper.data.results.SearchResult

class ContactSearchViewHolder(
    itemView: View,
    val isOnCard: Boolean
) : SearchViewHolder(itemView) {

    val icon = itemView.findViewById<ImageView>(R.id.icon)!!
    val text = itemView.findViewById<TextView>(R.id.text)!!

    override fun onBind(result: SearchResult) {
        result as ContactResult
        Glide.with(itemView)
            .load(result.iconUri)
            .placeholder(ContextCompat.getDrawable(itemView.context, R.drawable.placeholder_contact)!!.apply {
                setTint(if (isOnCard) ColorTheme.cardHint else ColorTheme.uiHint)
            })
            .apply(RequestOptions.circleCropTransform())
            .into(icon)
        text.text = result.title
        text.setTextColor(if (isOnCard) ColorTheme.cardTitle else ColorTheme.uiTitle)
        itemView.setOnClickListener(result::open)
    }
}