package io.posidon.android.cintalauncher.providers.summary

import android.content.Context
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import io.posidon.android.cintalauncher.data.feed.summary.SummaryItem
import io.posidon.android.cintalauncher.data.feed.summary.notification.NotificationSummary
import io.posidon.android.cintalauncher.providers.notification.NotificationCreator
import java.time.Instant

object NotificationSummaryCreator {

    fun create(
        context: Context,
        notification: StatusBarNotification,
        messagingStyle: NotificationCompat.MessagingStyle
    ): SummaryItem {

        val extras = notification.notification.extras

        val source = messagingStyle.conversationTitle?.toString() ?: NotificationCreator.getSource(context, notification)
        val text = NotificationCreator.getText(extras) ?: NotificationCreator.getTitle(extras)
        val icon = NotificationCreator.getSmallIcon(context, notification)
        val instant = Instant.ofEpochMilli(notification.postTime)
        val color = NotificationCreator.getColor(notification)

        //println(extras.keySet().joinToString("\n") { "$it -> " + extras[it].toString() })

        return NotificationSummary(
            color = color,
            sourceIcon = icon,
            description = text?.toString(),
            source = source,
            instant = instant,
            onTap = {
                try { notification.notification.contentIntent?.send() }
                catch (e: Exception) {
                    notification.notification.deleteIntent?.send()
                    e.printStackTrace()
                }
            }
        )
    }

    fun createCompressed(context: Context, notifications: List<StatusBarNotification>): SummaryItem {
        val first = notifications[0]

        val source = NotificationCreator.getSource(context, first)
        val text = run {
            val a = ArrayList<String?>()
            notifications.mapTo(a) {
                val m = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(it.notification)
                m?.conversationTitle?.toString()
            }
            a.removeIf { it == null }
            a.joinToString(", ", prefix = "${notifications.size} | ")
        }
        val icon = NotificationCreator.getSmallIcon(context, first)
        val instant = Instant.ofEpochMilli(notifications.maxOf { it.postTime })
        val color = NotificationCreator.getColor(first)

        return NotificationSummary(
            color = color,
            sourceIcon = icon,
            description = text,
            source = source,
            instant = instant,
            onTap = ({
                context.startActivity(context.packageManager.getLaunchIntentForPackage(first.packageName))
            }),
        )
    }
}