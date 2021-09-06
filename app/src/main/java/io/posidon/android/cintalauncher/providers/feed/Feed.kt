package io.posidon.android.cintalauncher.providers.feed

import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.storage.Settings
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

class Feed {

    companion object {
        const val MAX_ITEMS_HINT = 36
    }

    fun update() {
        thread(name = "Feed update thread", isDaemon = true) {
            val items = lock.withLock {
                ArrayList<FeedItem>().apply {
                    providers.forEach { addAll(it.get()) }
                }
            }
            onUpdate(FeedSorter.rearrange(items))
        }
    }

    private var onUpdate: (List<FeedItem>) -> Unit = {}
    private lateinit var providers: Array<out FeedItemProvider>
    private val lock = ReentrantLock()

    fun init(settings: Settings, vararg providers: FeedItemProvider, onUpdate: (List<FeedItem>) -> Unit, onDone: () -> Unit) {
        this.onUpdate = onUpdate
        this.providers = providers
        providers.forEach { it.preInit(this) }
        thread(name = "Feed init thread") {
            lock.withLock {
                providers.forEach { it.init(settings) }
            }
            onDone()
        }
    }
}