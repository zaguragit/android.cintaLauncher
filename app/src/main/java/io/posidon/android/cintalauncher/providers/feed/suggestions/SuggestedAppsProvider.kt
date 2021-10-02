package io.posidon.android.cintalauncher.providers.feed.suggestions

import android.view.View
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.data.feed.items.FeedItemSuggestedApps
import io.posidon.android.cintalauncher.data.feed.items.longHash
import io.posidon.android.cintalauncher.providers.feed.FeedItemProvider
import java.time.Instant

class SuggestedAppsProvider: FeedItemProvider() {

    override fun getUpdated(): List<FeedItem> {
        return SuggestionsManager.getSuggestions().let {
            it.subList(0, it.size.coerceAtMost(6))
        }.let {
            object : FeedItemSuggestedApps {
                override val apps = it
                override val color = 0
                override val title = "appss___"
                override val sourceIcon = null
                override val description = null
                override val source = null
                override val instant = Instant.MAX
                override fun onTap(view: View) {}
                override val isDismissible = false
                override val uid = "suggestions"
                override val id = uid.longHash()
            }
        }.let(::listOf)
    }
}