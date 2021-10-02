package io.posidon.android.cintalauncher.providers.feed.media

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.feed.items.FeedItemMeta
import io.posidon.android.cintalauncher.data.feed.items.FeedItemWithMedia
import io.posidon.android.cintalauncher.data.feed.items.longHash
import java.time.Instant

object MediaItemCreator {

    fun create(context: Context, controller: MediaController, mediaMetadata: MediaMetadata): FeedItemWithMedia {

        val title = mediaMetadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE)
        val subtitle = mediaMetadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
            ?: mediaMetadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: mediaMetadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: mediaMetadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)

        val coverBmp = mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
            ?: mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: null

        val albumBmp = mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: null

        val uid = mediaMetadata.getString(MediaMetadata.METADATA_KEY_MEDIA_ID)
            ?: null

        val cover = coverBmp ?: ContextCompat.getDrawable(context, R.drawable.ic_play)!!

        val color = coverBmp?.let {
            Palette.from(it).generate().run {
                getDominantColor(getVibrantColor(0xff000000.toInt()))
            }
        } ?: 0

        val meta = FeedItemMeta(
            sourcePackageName = controller.packageName
        )

        val applicationInfo = context.packageManager.getApplicationInfo(controller.packageName, 0)

        val label = mediaMetadata.getString(MediaMetadata.METADATA_KEY_ALBUM)
            ?: applicationInfo.loadLabel(context.packageManager)

        val sourceIcon = albumBmp?.let { BitmapDrawable(context.resources, it) }
            ?: applicationInfo.loadIcon(context.packageManager)
            ?: ContextCompat.getDrawable(context, R.drawable.ic_play)!!

        return object : FeedItemWithMedia {
            override val color = color
            override val title = title.toString()

            override val sourceIcon = sourceIcon
            override val shouldTintIcon = false
            override val description = subtitle?.toString()
            override val source = label.toString()
            override val instant = Instant.MAX
            override fun onTap(view: View) {
                controller.sessionActivity?.send()
            }

            override val isDismissible = false

            override fun previous(v: View) {
                controller.transportControls.skipToPrevious()
            }
            override fun next(v: View) {
                controller.transportControls.skipToNext()
            }
            override fun togglePause(v: ImageView) {
                if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
                    controller.transportControls.pause()
                    v.setImageResource(R.drawable.ic_play)
                } else {
                    controller.transportControls.play()
                    v.setImageResource(R.drawable.ic_pause)
                }
            }
            override fun isPlaying(): Boolean {
                return controller.playbackState?.state == PlaybackState.STATE_PLAYING
            }

            override val image = cover
            override val uid = uid ?: "media"
            override val id = uid?.longHash() ?: 123456
            override val meta = meta
        }
    }
}