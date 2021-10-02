package io.posidon.android.cintalauncher.providers.feed.notification

import android.content.Context
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.providers.feed.FeedItemProvider

class MediaProvider(val context: Context) : FeedItemProvider() {

    override fun onInit() {
        NotificationService.init(context)
        NotificationService.setOnUpdate(javaClass.name, ::update)
    }

    override fun getUpdated(): List<FeedItem> {
        return NotificationService.mediaItem?.let(::listOf) ?: emptyList()
    }
}
