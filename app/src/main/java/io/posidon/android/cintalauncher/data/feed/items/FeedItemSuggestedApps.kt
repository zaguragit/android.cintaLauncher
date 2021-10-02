package io.posidon.android.cintalauncher.data.feed.items

import io.posidon.android.cintalauncher.data.items.LauncherItem

interface FeedItemSuggestedApps : FeedItem {
    val apps: List<LauncherItem>
}