package io.posidon.android.cintalauncher.ui.feed.items.viewHolders

import android.content.res.ColorStateList
import android.graphics.*
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.toXfermode
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.providers.color.theme.ColorTheme
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.data.feed.items.FeedItemWithMedia

class FeedItemMediaViewHolder(
    itemView: View
) : FeedItemViewHolder(itemView) {

    val cover = itemView.findViewById<ImageView>(R.id.image)

    val previous = itemView.findViewById<ImageView>(R.id.button_previous)!!
    val play = itemView.findViewById<ImageView>(R.id.button_play)!!
    val next = itemView.findViewById<ImageView>(R.id.button_next)!!

    override fun onBind(item: FeedItem, color: Int) {
        super.onBind(item, color)

        item as FeedItemWithMedia

        val c = item.image as? Bitmap
        if (c == null)
            cover.setImageDrawable(null)
        else {
            val paint = Paint().apply {
                shader = LinearGradient(
                    c.width.toFloat() / 2f,
                    0f,
                    c.width.toFloat(),
                    0f,
                    intArrayOf(
                        0,
                        0x33000000.toInt(),
                        0x88000000.toInt(),
                        0xdd000000.toInt(),
                        0xff000000.toInt()
                    ),
                    floatArrayOf(
                        0f, .25f, .5f, .75f, 1f
                    ),
                    Shader.TileMode.CLAMP
                )
                xfermode = PorterDuff.Mode.DST_IN.toXfermode()
            }
            val paint2 = Paint().apply {
                shader = LinearGradient(
                    0f,
                    0f,
                    c.width.toFloat() * 1.5f,
                    0f,
                    0,
                    item.color,
                    Shader.TileMode.CLAMP
                )
                alpha = 100
                xfermode = PorterDuff.Mode.DST_OVER.toXfermode()
            }
            val w = c.width * 1.5f
            val bitmap = Bitmap.createBitmap(w.toInt(), c.height, c.config).applyCanvas {
                val x = (width - c.width).toFloat()
                drawBitmap(c, x, 0f, paint2)
                drawRect(x, 0f, c.width.toFloat(), height.toFloat(), paint)
                drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint2)
            }
            cover.setImageBitmap(bitmap)
        }


        cover.setOnClickListener(item::onTap)
        title.setOnClickListener(item::onTap)
        description.setOnClickListener(item::onTap)

        val titleColor = ColorUtils.blendARGB(ColorTheme.adjustColorForContrast(ColorTheme.uiBG, item.color), ColorTheme.uiTitle, .7f)
        val titleTintList = ColorStateList.valueOf(titleColor)

        previous.imageTintList = titleTintList
        play.imageTintList = titleTintList
        next.imageTintList = titleTintList

        play.setImageResource(if (item.isPlaying()) R.drawable.ic_pause else R.drawable.ic_play)

        previous.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            item.previous(it)
        }
        next.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            item.next(it)
        }
        play.setOnClickListener {
            it as ImageView
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            item.togglePause(it)
        }
    }
}