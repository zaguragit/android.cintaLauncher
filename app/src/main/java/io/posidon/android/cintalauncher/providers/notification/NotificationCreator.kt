package io.posidon.android.cintalauncher.providers.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.view.View
import androidx.core.app.NotificationManagerCompat
import io.posidon.android.cintalauncher.data.feed.items.*
import java.time.Instant

object NotificationCreator {

    inline fun getSmallIcon(context: Context, n: StatusBarNotification): Drawable? {
        return n.notification.smallIcon?.loadDrawable(context) ?: n.notification.getLargeIcon()?.loadDrawable(context)
    }

    inline fun getLargeIcon(context: Context, n: StatusBarNotification): Drawable? {
        return n.notification.getLargeIcon()?.loadDrawable(context)
    }

    inline fun getSource(context: Context, n: StatusBarNotification): String {
        return context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(n.packageName, 0)).toString()
    }

    inline fun getColor(n: StatusBarNotification): Int {
        return n.notification.color
    }

    inline fun getTitle(context: Context, n: StatusBarNotification, extras: Bundle): CharSequence? {
        var title = extras.getCharSequence(Notification.EXTRA_TITLE)
        if (title == null || title.toString().replace(" ", "").isEmpty()) {
            try { title = context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(n.packageName, 0)) }
            catch (e: Exception) { e.printStackTrace() }
        }
        return title
    }

    inline fun getText(extras: Bundle): CharSequence? {
        //if (isSummary) return extras.getCharSequence(Notification.EXTRA_TEXT)
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        return if (messages == null) {
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_TEXT)
        } else buildString {
            messages.forEach {
                val bundle = it as Bundle
                appendLine(bundle.getCharSequence("text"))
            }
            delete(lastIndex, length)
        }
    }

    inline fun getBigImage(context: Context, extras: Bundle): Drawable? {
        val b = extras[Notification.EXTRA_PICTURE] as Bitmap?
        if (b != null) {
            try {
                val d = BitmapDrawable(context.resources, b)
                if (b.width < 64 || b.height < 64) {
                    return null
                }
                return d
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    inline fun getImportance(importance: Int): Int {
        return when (importance) {
            NotificationManager.IMPORTANCE_NONE,
            NotificationManager.IMPORTANCE_MIN -> -1
            NotificationManager.IMPORTANCE_LOW,
            NotificationManager.IMPORTANCE_DEFAULT -> 0
            NotificationManager.IMPORTANCE_HIGH -> 1
            NotificationManager.IMPORTANCE_MAX -> 2
            else -> throw IllegalStateException("Invalid notification importance")
        }
    }

    fun create(context: Context, notification: StatusBarNotification): FeedItem {

        val extras = notification.notification.extras

        val title = getTitle(context, notification, extras)
        val text = getText(extras)
        val icon = getSmallIcon(context, notification)
        val source = getSource(context, notification)

        //println(extras.keySet().joinToString("\n") { "$it -> " + extras[it].toString() })

        val instant = Instant.ofEpochMilli(notification.postTime)

        val progress = extras.getInt(Notification.EXTRA_PROGRESS, -1)
        val maxProgress = extras.getInt(Notification.EXTRA_PROGRESS_MAX, -1)
        val intermediate = extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)

        val color = getColor(notification)

        val channel = NotificationManagerCompat.from(context).getNotificationChannel(notification.notification.channelId)
        val importance = channel?.importance?.let { getImportance(it) } ?: 0

        val actions = notification.notification.actions?.map { action ->
            FeedItemAction(action.title.toString()) {
                action.actionIntent.send()
            }
        }?.toTypedArray() ?: emptyArray()

        val id = notification.id.toLong() shl 32 or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) notification.uid.toLong()
            else notification.packageName.hashCode().toLong()

        val uid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            "⍾$id"
            else "${notification.packageName}⍾${notification.id}"

        if (importance == -1) {
            return object : FeedItemSmall {
                override val color = color
                override val title = title.toString()
                override val sourceIcon = icon
                override val description = text?.toString()
                override val source = source
                override val actions = actions
                override val instant = instant
                override val importance = importance.coerceAtLeast(0)
                override val isNotification = true
                override fun onTap(view: View) {
                    notification.notification.contentIntent?.send()
                }
                override val uid = uid
                override val id = id
            }
        }

        if (maxProgress > 0 || intermediate) {
            return object : FeedItemWithProgress {
                override val max = maxProgress
                override val progress = progress
                override val isIntermediate = intermediate
                override val color = color
                override val title = title.toString()
                override val sourceIcon = icon
                override val description = text?.toString()
                override val source = source
                override val actions = actions
                override val instant = instant
                override val importance = importance
                override val isNotification = true
                override fun onTap(view: View) {
                    notification.notification.contentIntent?.send()
                }
                override val uid = uid
                override val id = id
            }
        }

        val bigPic = getBigImage(context, extras)

        if (bigPic != null) {
            return object : FeedItemWithBigImage {
                override val image: Drawable = bigPic
                override val color = color
                override val title = title.toString()
                override val sourceIcon = icon
                override val description = text?.toString()
                override val source = source
                override val actions = actions
                override val instant = instant
                override val importance = importance
                override val isNotification = true
                override fun onTap(view: View) {
                    notification.notification.contentIntent?.send()
                }
                override val uid = uid
                override val id = id
            }
        }

        return object : FeedItem {
            override val color = color
            override val title = title.toString()
            override val sourceIcon = icon
            override val description = text?.toString()
            override val source = source
            override val actions = actions
            override val instant = instant
            override val importance = importance
            override val isNotification = true
            override fun onTap(view: View) {
                notification.notification.contentIntent?.send()
            }
            override val uid = uid
            override val id = id
        }
    }
}