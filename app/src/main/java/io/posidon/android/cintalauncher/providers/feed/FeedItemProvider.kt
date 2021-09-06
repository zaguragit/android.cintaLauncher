package io.posidon.android.cintalauncher.providers.feed

import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.storage.Settings

abstract class FeedItemProvider {
    open fun onInit() {}
    abstract fun getUpdated(): List<FeedItem>

    protected lateinit var feed: Feed
        private set
    private var itemCache: List<FeedItem>? = null
    protected inline val settings: Settings
        get() = feed.settings

    fun get(): List<FeedItem> = itemCache ?: getUpdated().also { itemCache = it }

    fun preInit(feed: Feed) {
        this.feed = feed
    }

    fun init() {
        itemCache = getUpdated()
        onInit()
    }

    fun update() {
        itemCache = getUpdated()
        feed.update()
    }
}