package io.posidon.android.cintalauncher.providers.feed

import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.storage.Settings

abstract class FeedItemProvider {
    open fun onInit() {}
    abstract fun getUpdated(): List<FeedItem>

    private lateinit var feed: Feed
    private var itemCache: List<FeedItem>? = null
    lateinit var settings: Settings

    fun get(): List<FeedItem> = itemCache ?: getUpdated().also { itemCache = it }

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