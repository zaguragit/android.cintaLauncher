package io.posidon.android.cintalauncher.providers.summary

import android.content.Context
import io.posidon.android.cintalauncher.data.feed.summary.SummaryItem
import io.posidon.android.cintalauncher.providers.notification.NotificationService

object NotificationSummariesProvider {

    private var lastSummariesList = emptyList<SummaryItem>()
    private var lastFullSummariesList = emptyList<SummaryItem>()
    private var lastMediaItem: SummaryItem? = null

    fun init(context: Context, onUpdate: () -> Unit) {
        NotificationService.init(context)
        NotificationService.setOnSummaryUpdate(onUpdate)
    }

    fun get(): List<SummaryItem> {
        val newSummariesList = NotificationService.notificationSummaries
        val m = NotificationService.mediaItem
        if (newSummariesList === lastSummariesList && m == lastMediaItem) {
            return lastFullSummariesList
        }
        lastMediaItem = m
        lastSummariesList = newSummariesList
        lastFullSummariesList = if (m == null) newSummariesList else newSummariesList + m
        return lastFullSummariesList
    }
}
