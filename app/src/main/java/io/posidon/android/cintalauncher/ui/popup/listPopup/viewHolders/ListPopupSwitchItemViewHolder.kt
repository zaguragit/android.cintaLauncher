package io.posidon.android.cintalauncher.ui.popup.listPopup.viewHolders

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.*
import android.util.StateSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.ui.feed.items.viewHolders.applyIfNotNull
import io.posidon.android.cintalauncher.ui.popup.listPopup.ListPopupItem
import posidon.android.conveniencelib.dp

class ListPopupSwitchItemViewHolder(itemView: View) : ListPopupViewHolder(itemView) {

    val icon = itemView.findViewById<ImageView>(R.id.icon)

    val text = itemView.findViewById<TextView>(R.id.text)
    val description = itemView.findViewById<TextView>(R.id.description)

    val switch = itemView.findViewById<SwitchCompat>(R.id.toggle)

    val ripple = RippleDrawable(ColorStateList.valueOf(0), null, ColorDrawable(0xffffffff.toInt()))

    init {
        itemView.background = ripple
    }

    override fun onBind(item: ListPopupItem) {
        text.text = item.text
        description.text = item.description

        itemView.setOnClickListener {
            switch.toggle()
        }

        text.setTextColor(ColorTheme.cardTitle)
        switch.trackDrawable = generateTrackDrawable(itemView.context)
        switch.thumbDrawable = generateThumbDrawable(itemView.context)

        ripple.setColor(ColorStateList.valueOf(ColorTheme.accentColor and 0xffffff or 0x33000000))

        applyIfNotNull(description, item.description) { view, value ->
            view.text = value
            description.setTextColor(ColorTheme.cardDescription)
        }
        applyIfNotNull(icon, item.icon) { view, value ->
            view.setImageDrawable(value)
            view.imageTintList = ColorStateList.valueOf(ColorTheme.cardDescription)
        }
        switch.setOnCheckedChangeListener(item.onToggle!!)
    }

    private fun generateTrackDrawable(context: Context): Drawable {
        val out = StateListDrawable()
        out.addState(intArrayOf(android.R.attr.state_checked), generateBG(context, ColorTheme.accentColor and 0x00ffffff or 0x55000000))
        out.addState(StateSet.WILD_CARD, generateBG(context, ColorTheme.cardHint and 0x00ffffff or 0x55000000))
        return out
    }

    private fun generateThumbDrawable(context: Context): Drawable {
        val out = StateListDrawable()
        out.addState(intArrayOf(android.R.attr.state_checked), generateCircle(context, ColorTheme.accentColor))
        out.addState(StateSet.WILD_CARD, generateCircle(context, ColorTheme.cardHint))
        return out
    }

    fun generateCircle(context: Context, color: Int): Drawable {
        val r = context.dp(20).toInt()
        val inset = context.dp(3).toInt()
        return LayerDrawable(arrayOf(
            GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
                setSize(r, r)
                setStroke(1, 0xdd000000.toInt())
            },
            GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                val highlight = Color.HSVToColor(floatArrayOf(0f, 0f, 0f).apply {
                    Color.colorToHSV(color, this)
                    this[2] *= 1.8f * (1 + this[2] * this[2])
                    this[1] *= 0.4f
                }) and 0x00ffffff
                colors = intArrayOf(highlight or 0x55000000, Color.HSVToColor(floatArrayOf(0f, 0f, 0f).apply {
                    Color.colorToHSV(color, this)
                    this[1] *= 1.08f
                    this[2] *= 1.6f
                }) and 0x00ffffff)
                setSize(r, r)
                setStroke(context.dp(1).toInt(), highlight or 0x12000000)
            }
        )).apply {
            setLayerInset(0, inset, inset, inset, inset)
            setLayerInset(1, inset, inset, inset, inset)
        }
    }

    fun generateBG(context: Context, color: Int): Drawable {
        return GradientDrawable().apply {
            cornerRadius = Float.MAX_VALUE
            val shadow = Color.HSVToColor(floatArrayOf(0f, 0f, 0f).apply {
                Color.colorToHSV(color, this)
                this[2] *= 0.8f
                this[1] *= 1.08f
            }) and 0x00ffffff
            colors = intArrayOf(shadow or (color and 0xff000000.toInt()), color)
            setStroke(1, 0xdd000000.toInt())
        }
    }
}