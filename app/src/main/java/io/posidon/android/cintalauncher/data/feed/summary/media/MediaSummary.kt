package io.posidon.android.cintalauncher.data.feed.summary.media

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import io.posidon.android.cintalauncher.data.feed.summary.SummaryItem
import java.time.Instant

class MediaSummary(
    color: Int,
    title: String?,
    val subtitle: String?,
    instant: Instant,
    val cover: Drawable?,
    onTap: (View) -> Unit,
    val previous: (View) -> Unit,
    val next: (View) -> Unit,
    val togglePause: (ImageView) -> Unit,
    val isPlaying: () -> Boolean,
    val uid: String?,
    val packageName: String,
) : SummaryItem(color, title, instant, onTap)