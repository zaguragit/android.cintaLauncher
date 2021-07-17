package io.posidon.android.cintalauncher.ui.feed.home.summary

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.opengl.GLES30
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.appspell.shaderview.ShaderView
import com.appspell.shaderview.gl.params.ShaderParamsBuilder
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.feed.summary.SummaryItem
import io.posidon.android.cintalauncher.data.feed.summary.media.MediaSummary
import posidon.android.conveniencelib.Colors
import posidon.android.conveniencelib.toBitmap
import posidon.android.conveniencelib.vibrate

class MediaSummaryViewHolder(
    itemView: View
) : SummaryViewHolder(itemView) {

    val card = itemView.findViewById<CardView>(R.id.card)!!
    val title = card.findViewById<TextView>(R.id.title)!!
    val description = card.findViewById<TextView>(R.id.description)!!

    val cover = card.findViewById<ImageView>(R.id.cover)!!

    val previous = card.findViewById<ImageView>(R.id.button_previous)!!
    val play = card.findViewById<ImageView>(R.id.button_play)!!
    val next = card.findViewById<ImageView>(R.id.button_next)!!

    val shaderView = card.findViewById<ShaderView>(R.id.shader_view)!!

    init {
        shaderView.shaderParams = ShaderParamsBuilder()
            .addTexture2D("albedo", textureSlot = GLES30.GL_TEXTURE0)
            .addColor("background_color", 0)
            .addVec2f("resolution", floatArrayOf(shaderView.width.toFloat(), shaderView.height.toFloat()))
            .build()
    }

    private var lastCover: Drawable? = null
    private var lastUid: String? = null
    private var lastWidth: Int = -1
    private var lastHeight: Int = -1

    override fun onBind(summary: SummaryItem) {
        summary as MediaSummary
        title.text = summary.description
        description.text = summary.subtitle

        cover.setOnClickListener(summary.onTap)
        title.setOnClickListener(summary.onTap)
        description.setOnClickListener(summary.onTap)

        val backgroundColor = summary.color
        val titleColor = ColorTheme.titleColorForBG(itemView.context, backgroundColor)
        val textColor = ColorTheme.textColorForBG(itemView.context, backgroundColor)

        if (lastUid != summary.uid || summary.uid == null && summary.cover != lastCover) {
            lastCover = summary.cover
            lastUid = summary.uid
            shaderView.shaderParams?.updateValue(
                "albedo",
                summary.cover?.toBitmap(duplicateIfBitmapDrawable = true),
                textureSlot = GLES30.GL_TEXTURE0,
                needToRecycle = false
            )
            shaderView.shaderParams?.updateValue(
                "background_color",
                floatArrayOf(
                    Colors.red(backgroundColor) / 255f,
                    Colors.green(backgroundColor) / 255f,
                    Colors.blue(backgroundColor) / 255f,
                    Colors.alpha(backgroundColor) / 255f
                )
            )
        }

        if (shaderView.width != lastWidth || shaderView.height != lastHeight) {
            lastWidth = shaderView.width
            lastHeight = shaderView.height
            shaderView.shaderParams?.updateValue(
                "resolution",
                floatArrayOf(shaderView.width.toFloat(), shaderView.height.toFloat())
            )
        }

        val titleTintList = ColorStateList.valueOf(titleColor)

        card.setCardBackgroundColor(backgroundColor)
        title.setTextColor(titleColor)
        title.setShadowLayer(title.shadowRadius, title.shadowDx, title.shadowDy, backgroundColor)
        description.setShadowLayer(description.shadowRadius, description.shadowDx, description.shadowDy, backgroundColor)
        description.setTextColor(textColor)
        previous.imageTintList = titleTintList
        play.imageTintList = titleTintList
        next.imageTintList = titleTintList

        play.setImageResource(if (summary.isPlaying()) R.drawable.ic_pause else R.drawable.ic_play)

        previous.setOnClickListener {
            it.context.vibrate(14)
            summary.previous(it)
        }
        next.setOnClickListener {
            it.context.vibrate(14)
            summary.next(it)
        }
        play.setOnClickListener {
            it as ImageView
            it.context.vibrate(14)
            summary.togglePause(it)
        }
    }
}