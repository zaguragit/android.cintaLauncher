package io.posidon.android.cintalauncher.providers.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.UserHandle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import io.posidon.android.cintalauncher.BuildConfig
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.data.feed.summary.SummaryItem
import io.posidon.android.cintalauncher.data.feed.summary.media.MediaSummary
import io.posidon.android.cintalauncher.providers.summary.MediaItemCreator
import io.posidon.android.cintalauncher.providers.summary.NotificationSummaryCreator
import io.posidon.android.cintalauncher.util.StackTraceActivity
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread


class NotificationService : NotificationListenerService() {

    val componentName = ComponentName(BuildConfig.APPLICATION_ID, this::class.java.name)

    override fun onCreate() {
        StackTraceActivity.init(applicationContext)
        val msm = getSystemService(MediaSessionManager::class.java)
        msm.addOnActiveSessionsChangedListener(::onMediaControllersUpdated, componentName)
        onMediaControllersUpdated(msm.getActiveSessions(componentName))
    }

    override fun onDestroy() {
        super.onDestroy()
        val msm = getSystemService(MediaSessionManager::class.java)
        msm.removeOnActiveSessionsChangedListener(::onMediaControllersUpdated)
    }

    override fun onListenerConnected() {
        loadNotifications(activeNotifications)
    }

    override fun onNotificationPosted(s: StatusBarNotification) = loadNotifications(activeNotifications)
    override fun onNotificationPosted(s: StatusBarNotification?, rm: RankingMap?) = loadNotifications(activeNotifications)
    override fun onNotificationRemoved(s: StatusBarNotification) = loadNotifications(activeNotifications)
    override fun onNotificationRemoved(s: StatusBarNotification?, rm: RankingMap?) = loadNotifications(activeNotifications)
    override fun onNotificationRemoved(s: StatusBarNotification, rm: RankingMap, reason: Int) = loadNotifications(activeNotifications)
    override fun onNotificationRankingUpdate(rm: RankingMap) = loadNotifications(activeNotifications)
    override fun onNotificationChannelModified(pkg: String, u: UserHandle, c: NotificationChannel, modifType: Int) = loadNotifications(activeNotifications)
    override fun onNotificationChannelGroupModified(pkg: String, u: UserHandle, g: NotificationChannelGroup, modifType: Int) = loadNotifications(activeNotifications)

    private fun loadNotifications(notifications: Array<StatusBarNotification>?) {
        thread(name = "NotificationService loading thread", isDaemon = true) {
            val tmpNotifications = ArrayList<FeedItem>()
            val tmpMessageNotifications = ArrayList<StatusBarNotification>()
            val tmpSummaries = ArrayList<SummaryItem>()
            var i = 0
            try {
                if (notifications != null) {
                    while (i < notifications.size) {
                        val notification = notifications[i]
                        val isSummary = notification.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
                        if (!isSummary) {
                            val isMusic = notification.notification.extras
                                .getCharSequence(Notification.EXTRA_TEMPLATE) == Notification.MediaStyle::class.java.name
                            if (isMusic) {
                                i++
                                continue
                            }
                            val messagingStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(
                                notification.notification)
                            if (messagingStyle != null) {
                                tmpMessageNotifications += notification
                            }
                            tmpNotifications += NotificationCreator.create(applicationContext, notification)
                        }
                        i++
                    }
                }
                tmpMessageNotifications.groupBy {
                    it.groupKey
                }.flatMapTo(tmpSummaries) { (_, notifications) ->
                    if (notifications.size == 1) {
                        notifications.map {
                            val messagingStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(it.notification)
                            if (messagingStyle != null) {
                                NotificationSummaryCreator.create(
                                    applicationContext,
                                    it,
                                    messagingStyle
                                )
                            } else throw Exception("Non-message notification got into the summary")
                        }
                    } else {
                        listOf(NotificationSummaryCreator.createCompressed(
                            applicationContext,
                            notifications
                        ))
                    }
                }
            }
            catch (e: Exception) { e.printStackTrace() }
            lock.lock()
            NotificationService.notifications = tmpNotifications
            notificationSummaries = tmpSummaries
            onUpdate()
            onSummaryUpdate()
            lock.unlock()
        }
    }

    private fun onMediaControllersUpdated(controllers: MutableList<MediaController>?) {
        val old = mediaItem
        if (controllers.isNullOrEmpty()) {
            mediaItem = null
            if (old != mediaItem) {
                onSummaryUpdate()
            }
            return
        }
        val controller = pickController(controllers)
        mediaItem = controller.metadata?.let { MediaItemCreator.create(applicationContext, controller, it) }
        if (old != mediaItem) {
            onSummaryUpdate()
        }
        controller.registerCallback(object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) {
                mediaItem = metadata?.let { MediaItemCreator.create(applicationContext, controller, it) }
                onSummaryUpdate()
            }
        })
    }

    companion object {
        fun init(context: Context) {
            context.startService(Intent(context, NotificationService::class.java))
        }

        var notifications = ArrayList<FeedItem>()
            private set

        var notificationSummaries = ArrayList<SummaryItem>()
            private set

        var mediaItem: MediaSummary? = null
            private set

        private var onUpdate: () -> Unit = {}
        private var onSummaryUpdate: () -> Unit = {}

        private val lock = ReentrantLock()

        private fun pickController(controllers: List<MediaController>): MediaController {
            for (i in controllers.indices) {
                val mc = controllers[i]
                if (mc.playbackState?.state == PlaybackState.STATE_PLAYING) {
                    return mc
                }
            }
            return controllers[0]
        }

        fun setOnUpdate(onUpdate: () -> Unit) {
            this.onUpdate = onUpdate
        }

        fun setOnSummaryUpdate(onSummaryUpdate: () -> Unit) {
            this.onSummaryUpdate = onSummaryUpdate
        }
    }
}