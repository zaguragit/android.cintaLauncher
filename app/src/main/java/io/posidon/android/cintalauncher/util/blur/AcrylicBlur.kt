package io.posidon.android.cintalauncher.util.blur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import posidon.android.conveniencelib.Device
import posidon.android.conveniencelib.Graphics
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.toBitmap
import kotlin.concurrent.thread

class AcrylicBlur private constructor(
    val fullBlur: Bitmap,
    val smoothBlur: Bitmap,
    val partialBlurMedium: Bitmap,
    val partialBlurSmall: Bitmap
) {

    fun recycle() {
        fullBlur.recycle()
        smoothBlur.recycle()
        partialBlurMedium.recycle()
        partialBlurSmall.recycle()
    }

    companion object {
        fun blurWallpaper(context: Context, drawable: Drawable): AcrylicBlur {
            val w = Device.screenWidth(context)
            val h = Device.screenHeight(context)

            val start = System.currentTimeMillis()

            val b = drawable.toBitmap(w / 12, h / 12)
            val smoothBlur = Graphics.fastBlur(b, context.dp(1f).toInt())

            val partialBlurMedium = Graphics.fastBlur(b, context.dp(.6f).toInt())
            val partialBlurSmall = Graphics.fastBlur(b, context.dp(.3f).toInt())

            val blurT = System.currentTimeMillis()

            val nb = Bitmap.createScaledBitmap(smoothBlur, w, h, false)

            val scaleT = System.currentTimeMillis()

            val fullBlur = NoiseBlur.blur(nb, context.dp(18f))

            val noiseT = System.currentTimeMillis()
            println("""
                blur: ${blurT - start}
                scale: ${scaleT - blurT}
                noise: ${noiseT - scaleT}
            """.trimIndent())
            return AcrylicBlur(fullBlur, smoothBlur, partialBlurMedium, partialBlurSmall)
        }

        inline fun blurWallpaper(
            context: Context,
            drawable: Drawable,
            crossinline onEnd: (AcrylicBlur) -> Unit
        ): Thread = thread(isDaemon = true) {
            onEnd(blurWallpaper(context, drawable))
        }
    }
}