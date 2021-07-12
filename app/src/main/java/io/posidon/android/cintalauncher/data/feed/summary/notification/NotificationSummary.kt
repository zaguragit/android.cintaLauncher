package io.posidon.android.cintalauncher.data.feed.summary.notification

import android.graphics.drawable.Drawable
import android.view.View
import io.posidon.android.cintalauncher.data.feed.summary.SummaryItem
import java.time.Instant

class NotificationSummary(
    color: Int,
    val sourceIcon: Drawable?,
    description: String?,
    val source: String,
    instant: Instant,
    onTap: (View) -> Unit
) : SummaryItem(color, description, instant, onTap)