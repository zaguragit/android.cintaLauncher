package io.posidon.android.cintalauncher.providers

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Process
import io.posidon.android.cintalauncher.LauncherContext
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.storage.Settings
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

class SuggestionsManager {

    private var appOpeningContexts = HashMap<LauncherItem, List<FloatArray>>()

    private var suggestions = emptyList<LauncherItem>()

    fun getSuggestions(): List<LauncherItem> = suggestions

    fun onItemOpened(context: Context, item: LauncherItem) {
        thread (isDaemon = true, name = "SuggestionManager: suggestions update") {
            saveItemOpenContext(context, item)
            updateSuggestions(context)
        }
    }

    private fun saveItemOpenContext(context: Context, item: LauncherItem) {
        val data = FloatArray(CONTEXT_DATA_SIZE)
        getCurrentContext(context, data)
        appOpeningContexts[item] = appOpeningContexts[item]?.plus(data)?.let { trimContextListIfTooBig(it) } ?: listOf(data)
    }

    private fun trimContextListIfTooBig(list: List<FloatArray>): List<FloatArray> {
        return if (list.size > MAX_CONTEXT_COUNT) {
            list.sortedBy {
                -calculateDistance(it, list - it)
            }.subList(0, MAX_CONTEXT_COUNT)
        } else list
    }

    private fun getCurrentContext(context: Context, out: FloatArray) {
        val batteryManager = context.getSystemService(BatteryManager::class.java)
        val audioManager = context.getSystemService(AudioManager::class.java)
        val rightNow = Calendar.getInstance()

        val batteryLevel = batteryManager
            .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isPluggedIn = batteryManager
            .getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING
        val currentHourIn24Format = rightNow[Calendar.HOUR_OF_DAY] + rightNow[Calendar.MINUTE] / 60f + rightNow[Calendar.SECOND] / 60f / 60f
        val isWeekend = rightNow[Calendar.DAY_OF_WEEK].let {
            it == Calendar.SATURDAY || it == Calendar.SUNDAY
        }
        val isHeadSetConnected = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).isNotEmpty()
        val connManager = context.getSystemService(WifiManager::class.java)
        val isWifiOn = connManager!!.isWifiEnabled

        out[CONTEXT_DATA_HOUR_OF_DAY] = currentHourIn24Format / 12f
        out[CONTEXT_DATA_BATTERY] = batteryLevel / 100f
        out[CONTEXT_DATA_HAS_HEADSET] = if (isHeadSetConnected) 1f else 0f
        out[CONTEXT_DATA_HAS_WIFI] = if (isWifiOn) 1f else 0f
        out[CONTEXT_DATA_IS_PLUGGED_IN] = if (isPluggedIn) 1f else 0f
        out[CONTEXT_DATA_IS_WEEKEND] = if (isWeekend) 1f else 0f
    }

    fun onAppsLoaded(
        appManager: LauncherContext.AppManager,
        context: Context,
        settings: Settings
    ) {
        loadSavedRecents(settings, context, appManager)
        updateSuggestions(context)
    }

    private fun updateSuggestions(context: Context) {
        val stats = if (checkUsageAccessPermission(context)) {
            val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)

            val c = Calendar.getInstance()
            c.add(Calendar.HOUR_OF_DAY, -1)

            HashMap<String, UsageStats>().apply {
                usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    c.timeInMillis,
                    System.currentTimeMillis()
                ).forEach {
                    this[it.packageName] = it
                }
            }
        } else null

        val currentData = FloatArray(CONTEXT_DATA_SIZE)
        getCurrentContext(context, currentData)

        this.suggestions = run {
            val sortedEntries = appOpeningContexts.entries.sortedBy { (item, data) ->
                val timeF = if (stats != null) {
                    val lastUse = stats[(item as App).packageName]?.lastTimeUsed
                    if (lastUse == null) 1f else run {
                        val c = Calendar.getInstance()
                        c.timeInMillis = System.currentTimeMillis() - lastUse
                        (c[Calendar.MINUTE] / 20f).coerceAtMost(1f).pow(2)
                    }
                } else 0f

                calculateDistance(currentData, data) + timeF
            }
            sortedEntries
                .subList(0, sortedEntries.size.coerceAtMost(3))
                .map { it.key }
        }
    }

    private val lengthBuffer = FloatArray(CONTEXT_DATA_SIZE)
    fun calculateDistance(currentContext: FloatArray, multipleContexts: List<FloatArray>): Float {
        return multipleContexts.map { d ->
            currentContext.forEachIndexed { i, fl ->
                val base = abs(fl - d[i])
                lengthBuffer[i] = if (i == CONTEXT_DATA_HOUR_OF_DAY)
                    min(base, 24 - base)
                else base
                lengthBuffer[i] *= lengthBuffer[i]
            }
            lengthBuffer.sum()
        }.reduce(Float::times)
    }

    fun loadSavedRecents(
        settings: Settings,
        context: Context,
        appManager: LauncherContext.AppManager
    ) {
        settings.getStrings("stats:app_opening_contexts")?.let {
            val appOpeningContexts = HashMap<LauncherItem, List<FloatArray>>()
            it.forEach { packageName ->
                appManager.parseLauncherItem(packageName)?.let { item ->
                    settings.getStrings("stats:app_opening_context:$packageName")
                        ?.map(String::toFloat)?.let { floats ->
                        val data = LinkedList<FloatArray>()
                        repeat (floats.size / CONTEXT_DATA_SIZE) { i ->
                            data += floats.subList(i * CONTEXT_DATA_SIZE, (i + 1) * CONTEXT_DATA_SIZE).toFloatArray()
                        }
                        appOpeningContexts[item] = data
                    }
                } ?: settings.edit(context) {
                    setStrings("stats:app_opening_context:$packageName", null)
                }
            }
            this.appOpeningContexts = appOpeningContexts
        }
    }

    fun onResume(context: Context, appManager: LauncherContext.AppManager) {
        thread (isDaemon = true, name = "SuggestionManager: onResume") {
            updateSuggestions(context)
        }
    }

    fun save(settings: Settings, context: Context) {
        settings.edit(context) {
            "stats:app_opening_contexts" set appOpeningContexts
                .map { it.key.toString() }
                .toTypedArray()
            appOpeningContexts.forEach { (packageName, data) ->
                "stats:app_opening_context:$packageName" set data
                    .flatMap(FloatArray::toList)
                    .map(Float::toString)
                    .toTypedArray()
            }
        }
    }

    companion object {
        private const val CONTEXT_DATA_HOUR_OF_DAY = 0
        private const val CONTEXT_DATA_BATTERY = 1
        private const val CONTEXT_DATA_HAS_HEADSET = 2
        private const val CONTEXT_DATA_HAS_WIFI = 3
        private const val CONTEXT_DATA_IS_PLUGGED_IN = 4
        private const val CONTEXT_DATA_IS_WEEKEND = 5
        private const val CONTEXT_DATA_SIZE = 6
        private const val MAX_CONTEXT_COUNT = 3

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