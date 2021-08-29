package io.posidon.android.cintalauncher.providers

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import io.posidon.android.cintalauncher.LauncherContext
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.storage.Settings
import java.util.*


class AppSuggestionsManager {

    private var last3 = LinkedList<LauncherItem>()
    private var last3System = emptyList<LauncherItem>()

    private var hasPermission: Boolean = false

    fun getLast3(): List<LauncherItem> = if (hasPermission) last3System else last3

    fun onItemOpened(item: LauncherItem) {
        last3.removeAll { it == item }
        last3.addFirst(item)
        while (last3.size > 3) last3.removeLast()
    }

    fun onAppsLoaded(
        appManager: LauncherContext.AppManager,
        context: Context,
        settings: Settings
    ) {
        loadSavedRecents(settings, appManager)
        tryLoadFromSystem(context, appManager)
    }

    fun loadSavedRecents(settings: Settings, appManager: LauncherContext.AppManager) {
        settings.getStrings("stats:recently_opened")?.let {
            val last3 = LinkedList<LauncherItem>()
            it.forEach {
                appManager.parseLauncherItem(it)?.let { it1 -> last3.add(it1) }
            }
            this.last3 = last3
        }
    }

    fun tryLoadFromSystem(context: Context, appManager: LauncherContext.AppManager) {
        hasPermission = checkUsageAccessPermission(context)
        if (hasPermission) {
            loadSystemRecents(context, appManager)
        }
    }

    fun loadSystemRecents(context: Context, appManager: LauncherContext.AppManager) {
        val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)

        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -5)

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, c.timeInMillis, System.currentTimeMillis())

        stats.sortByDescending {
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                it.lastTimeVisible
            else it.lastTimeUsed) + (it.totalTimeInForeground / 1000 / 20)
        }

        last3System = LinkedList<LauncherItem>().also {
            var i = 0
            for (stat in stats) {
                if (stat.packageName == context.packageName) continue
                it += appManager.getAppByPackage(stat.packageName) ?: continue
                i++
                if (i >= 3) break
            }
        }
    }

    fun onResume(context: Context, appManager: LauncherContext.AppManager) {
        hasPermission = checkUsageAccessPermission(context)
        if (hasPermission) {
            loadSystemRecents(context, appManager)
        }
    }

    fun save(settings: Settings, context: Context) {
        settings.edit(context) {
            "stats:recently_opened" set last3.map(LauncherItem::toString).toTypedArray()
        }
    }

    companion object {
        fun checkUsageAccessPermission(context: Context): Boolean {
            val aom = context.getSystemService(AppOpsManager::class.java)
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                aom.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(), context.packageName
                )
            } else aom.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.packageName
            )

            return if (mode == AppOpsManager.MODE_DEFAULT) {
                context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
            } else {
                mode == AppOpsManager.MODE_ALLOWED
            }
        }
    }
}