package io.posidon.android.cintalauncher.data.feed.profiles

import io.posidon.android.cintalauncher.data.feed.items.FeedItem

class ExtraFeedProfileSettings(
    val onlyTheseSources: List<String>?,
    val onlyThesePackages: List<String>?,
) {
    fun filter(item: FeedItem): Boolean {
        val packageName = item.meta?.sourcePackageName
        val source = item.meta?.sourceUrl

        if (onlyThesePackages != null && packageName != null) {
            return packageName in onlyThesePackages
        }

        if (onlyTheseSources != null && source != null) {
            return source in onlyTheseSources
        }

        return true
    }
}