package io.posidon.android.cintalauncher.data.feed.items

import android.graphics.drawable.Drawable
import android.view.View

class FeedItemAction(
    val text: String,
    val icon: Drawable? = null,
    val onTap: (view: View) -> Unit
)
