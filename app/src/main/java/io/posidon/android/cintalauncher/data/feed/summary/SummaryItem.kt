package io.posidon.android.cintalauncher.data.feed.summary

import android.view.View
import java.time.Instant

abstract class SummaryItem(
    val color: Int,
    val description: String?,
    val instant: Instant,
    val onTap: (View) -> Unit
)