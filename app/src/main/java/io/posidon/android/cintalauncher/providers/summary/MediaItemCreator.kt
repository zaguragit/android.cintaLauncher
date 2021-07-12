package io.posidon.android.cintalauncher.providers.summary

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.feed.summary.media.MediaSummary
import posidon.android.conveniencelib.toBitmap
import java.time.Instant

object MediaItemCreator {

    fun create(context: Context, controller: MediaController, meta: MediaMetadata): MediaSummary {

        val title = meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: meta.getString(MediaMetadata.METADATA_KEY_TITLE)
        val subtitle = meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
            ?: meta.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: meta.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)

        val coverBmp = meta.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
            ?: meta.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: meta.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: null

        val uid = meta.getString(MediaMetadata.METADATA_KEY_MEDIA_ID)
            ?: null

        val cover = coverBmp?.let { BitmapDrawable(context.resources, it) }
            ?: ContextCompat.getDrawable(context, R.drawable.ic_play)

        val instant = Instant.MAX

        val color = Palette.from(cover!!.toBitmap()).generate().run {
            getDominantColor(getVibrantColor(0xff000000.toInt()))
        }

        return MediaSummary(
            color = color,
            title = title?.toString(),
            subtitle = subtitle?.toString(),
            instant = instant,
            cover = cover,
            onTap = {
                controller.sessionActivity?.send()
            },
            previous = {
                controller.transportControls.skipToPrevious()
            },
            next = {
                controller.transportControls.skipToNext()
            },
            togglePause = {
                if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
                    controller.transportControls.pause()
                    it.setImageResource(R.drawable.ic_play)
                } else {
                    controller.transportControls.play()
                    it.setImageResource(R.drawable.ic_pause)
                }
            },
            isPlaying = {
                controller.playbackState?.state == PlaybackState.STATE_PLAYING
            },
            uid = uid
        )
    }
}