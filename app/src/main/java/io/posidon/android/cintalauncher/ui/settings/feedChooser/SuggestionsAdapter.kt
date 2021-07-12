package io.posidon.android.cintalauncher.ui.settings.feedChooser

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import io.posidon.android.cintalauncher.ui.color.ColorTheme
import posidon.android.conveniencelib.dp

class SuggestionsAdapter(
    val suggestions: Suggestions
) : BaseExpandableListAdapter() {

    override fun getGroupCount() = suggestions.topics.size
    override fun getChildrenCount(topicI: Int) = suggestions[topicI].sources.size
    override fun getGroup(topicI: Int) = suggestions[topicI]
    override fun getChild(topicI: Int, sourceI: Int) = suggestions[topicI].sources[sourceI]
    override fun getGroupId(topicI: Int) = topicI.toLong()
    override fun getChildId(topicI: Int, sourceI: Int) = sourceI.toLong()
    override fun hasStableIds() = false

    override fun getGroupView(topicI: Int, p1: Boolean, cv: View?, parent: ViewGroup): View {
        val convertView = (cv ?: TextView(parent.context).apply {
            val h = context.dp(16).toInt()
            val v = context.dp(8).toInt()
            setPadding(h, v + context.dp(8).toInt(), h, v)
            val textColor = ColorTheme.textColorForBG(context, ColorTheme.appDrawerColor)
            setTextColor(textColor)
            textSize = 20f
        }) as TextView

        convertView.text = suggestions[topicI].name
        return convertView
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun getChildView(topicI: Int, sourceI: Int, isLast: Boolean, cv: View?, parent: ViewGroup): View {
        val convertView = (cv ?: TextView(parent.context).apply {
            val h = context.dp(16).toInt()
            setPadding(h + context.dp(16).toInt(), context.dp(2).toInt(), h, context.dp(8).toInt())
            val textColor = ColorTheme.textColorForBG(context, ColorTheme.appDrawerColor)
            setTextColor(textColor)
            textSize = 16f
        }) as TextView

        convertView.text = suggestions[topicI][sourceI].name

        return convertView
    }

    override fun isChildSelectable(topicI: Int, sourceI: Int) = true
}
