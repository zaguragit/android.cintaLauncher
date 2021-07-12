package io.posidon.android.cintalauncher.providers

import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.storage.Settings

abstract class FeedItemProvider {
    open fun onInit() {}
    abstract fun getUpdated(): List<FeedItem>

    private lateinit var feed: Feed
    private lateinit var itemCache: List<FeedItem>
    lateinit var settings: Settings

    fun get(): List<FeedItem> = itemCache

    fun preInit(feed: Feed) {
        this.feed = feed
    }

    fun init(settings: Settings) {
        this.settings = settings
        itemCache = getUpdated()
        onInit()
    }

    fun update() {
        itemCache = getUpdated()
        feed.update()
    }
}