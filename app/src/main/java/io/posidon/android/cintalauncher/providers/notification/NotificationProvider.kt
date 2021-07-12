package io.posidon.android.cintalauncher.providers.notification

import android.content.Context
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.providers.FeedItemProvider

class NotificationProvider(val context: Context) : FeedItemProvider() {

    override fun onInit() {
        NotificationService.init(context)
        NotificationService.setOnUpdate(::update)
    }

    override fun getUpdated(): List<FeedItem> {
        return NotificationService.notifications
    }
}
