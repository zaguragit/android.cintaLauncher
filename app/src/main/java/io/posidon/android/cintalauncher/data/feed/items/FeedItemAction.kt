package io.posidon.android.cintalauncher.data.feed.items

import android.graphics.drawable.Drawable
import android.view.View

data class FeedItemAction(
    val text: String,
    val icon: Drawable? = null,
    val onTap: (view: View) -> Unit
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FeedItemAction

        if (text != other.text) return false
        if (icon?.constantState != other.icon?.constantState) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + (icon?.constantState?.hashCode() ?: 0)
        return result
    }
}
