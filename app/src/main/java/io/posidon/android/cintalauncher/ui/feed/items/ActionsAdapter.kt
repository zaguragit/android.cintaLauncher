package io.posidon.android.cintalauncher.ui.feed.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.feed.items.FeedItemAction
import io.posidon.android.cintalauncher.ui.feed.items.viewHolders.applyIfNotNull

class ActionsAdapter(
    private var actions: Array<FeedItemAction>,
    private var actionButtonTextColor: Int
) : RecyclerView.Adapter<ActionsAdapter.ActionViewHolder>() {

    override fun getItemCount(): Int = actions.size

    class ActionViewHolder(actionButtonTextColor: Int, itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon = itemView.findViewById<ImageView>(R.id.action_icon)!!
        val text = itemView.findViewById<TextView>(R.id.action_text)!!.apply {
            setTextColor(actionButtonTextColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        return ActionViewHolder(actionButtonTextColor, LayoutInflater.from(parent.context)
            .inflate(R.layout.feed_item_action, parent, false))
    }

    override fun onBindViewHolder(holder: ActionViewHolder, i: Int) {
        val action = actions[i]
        holder.text.text = action.text
        applyIfNotNull(holder.icon, action.icon, ImageView::setImageDrawable)
        holder.itemView.setOnClickListener(action.onTap)
    }
}
