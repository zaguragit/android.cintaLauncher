package io.posidon.android.cintalauncher.ui

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.graphics.toRectF
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.providers.AppSuggestionsManager
import io.posidon.android.cintalauncher.providers.Feed
import io.posidon.android.cintalauncher.providers.app.AppCallback
import io.posidon.android.cintalauncher.providers.app.AppCollection
import io.posidon.android.cintalauncher.providers.notification.NotificationProvider
import io.posidon.android.cintalauncher.providers.rss.RssProvider
import io.posidon.android.cintalauncher.storage.Settings
import io.posidon.android.cintalauncher.storage.colorTheme
import io.posidon.android.cintalauncher.ui.drawer.AppDrawer
import io.posidon.android.cintalauncher.ui.feed.items.FeedAdapter
import io.posidon.android.cintalauncher.ui.view.AlphabetScrollbar
import io.posidon.android.cintalauncher.util.InvertedRoundRectDrawable
import io.posidon.android.cintalauncher.util.StackTraceActivity
import io.posidon.android.launcherutils.AppLoader
import io.posidon.android.launcherutils.GestureNavContract
import io.posidon.android.launcherutils.LiveWallpaper
import posidon.android.conveniencelib.getNavigationBarHeight
import posidon.android.conveniencelib.getStatusBarHeight
import kotlin.concurrent.thread

class LauncherActivity : FragmentActivity() {

    val feed = Feed()
    val settings = Settings()

    val suggestionsManager = AppSuggestionsManager()

    val notificationProvider = NotificationProvider(this)

    val feedRecycler by lazy { findViewById<RecyclerView>(R.id.feed_recycler)!! }
    val scrollBar by lazy { findViewById<AlphabetScrollbar>(R.id.scroll_bar)!! }

    val appDrawer by lazy { AppDrawer(this, scrollBar) }

    lateinit var feedAdapter: FeedAdapter

    lateinit var wallpaperManager: WallpaperManager

    val appLoader = AppLoader(::App, ::AppCollection)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StackTraceActivity.init(applicationContext)
        setContentView(R.layout.activity_launcher)
        configureWindow()
        settings.init(applicationContext)
        ColorTheme.onCreate(this)
        wallpaperManager = WallpaperManager.getInstance(this)

        feedRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        feedAdapter = FeedAdapter(this)
        feedAdapter.setHasStableIds(true)
        feedRecycler.setItemViewCacheSize(20)
        feedRecycler.adapter = feedAdapter
        val r = resources.getDimension(R.dimen.dock_corner_radius)
        findViewById<View>(R.id.home_container).foreground = InvertedRoundRectDrawable(
            floatArrayOf(0f, 0f, 0f, 0f, r, r, r, r), 0f, 0xff000000.toInt())

        feed.init(settings, notificationProvider, RssProvider, onUpdate = this::loadFeed) {
            runOnUiThread {
                feedAdapter.onFeedInitialized()
            }
        }

        appDrawer.init()

        window.decorView.findViewById<View>(android.R.id.content).setOnTouchListener(::onTouch)

        val launcherApps = getSystemService(LauncherApps::class.java)
        launcherApps.registerCallback(AppCallback(::loadApps))

        loadApps()
        updateColorTheme(this, ColorTheme)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            wallpaperManager.addOnColorsChangedListener(::onColorsChangedListener, feedRecycler.handler)
            thread(name = "onCreate color update", isDaemon = true) {
                ColorTheme.onColorsChanged(this, settings.colorTheme, ::updateColorTheme) {
                    wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                }
            }
            onWallpaperChanged()
        }
    }

    private fun updateColorTheme(a: LauncherActivity, new: ColorTheme) {
        a.feedAdapter.updateColorTheme()
        a.appDrawer.updateColorTheme()
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    fun onColorsChangedListener(
        colors: WallpaperColors?,
        which: Int
    ) {
        if (which and WallpaperManager.FLAG_SYSTEM != 0) {
            onWallpaperChanged()
            ColorTheme.onColorsChanged(this, settings.colorTheme, ::updateColorTheme) { colors }
        }
    }

    override fun onBackPressed() {
        if (appDrawer.isOpen) appDrawer.close()
        else feedRecycler.scrollToPosition(0)
    }

    private var lastUpdateTime = System.currentTimeMillis()

    override fun onResume() {
        super.onResume()
        notificationProvider.update()
        val shouldUpdate = settings.reload(applicationContext)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            ColorTheme.onResumePreOMR1(this, settings.colorTheme, ::updateColorTheme)
            onWallpaperChanged()
        } else if (shouldUpdate) {
            ColorTheme.onColorsChanged(this, settings.colorTheme, ::updateColorTheme) { wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM) }
        }
        val current = System.currentTimeMillis()
        if (current - lastUpdateTime > 1000L * 60L * 5L) {
            lastUpdateTime = current
            thread (isDaemon = true, block = RssProvider::update)
        }
        if (shouldUpdate) {
            loadApps()
        } else {
            suggestionsManager.onResume(this)
        }
    }

    override fun onPause() {
        super.onPause()
        appDrawer.close()
        suggestionsManager.save(settings, this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            notificationProvider.update()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureWindow()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val isActionMain = Intent.ACTION_MAIN == intent.action
        if (isActionMain) {
            handleGestureContract(intent)
        }
    }

    private fun onWallpaperChanged() {

    }

    private fun handleGestureContract(intent: Intent) {
        val gnc = GestureNavContract.fromIntent(intent)
        gnc?.sendEndPosition(scrollBar.clipBounds.toRectF(), null)
    }

    fun loadFeed(items: List<FeedItem>) {
        runOnUiThread {
            feedAdapter.updateItems(items)
            Log.d("Cinta", "updated feed (${items.size} items)")
        }
    }

    fun loadApps() {
        appLoader.async(this, settings.getStrings("icon_packs") ?: emptyArray()) { apps: AppCollection ->
            appDrawer.update(apps.sections)
            suggestionsManager.onAppsLoaded(this, settings, apps.byName)
            runOnUiThread {
                feedAdapter.onAppsLoaded()
            }
            Log.d("Cinta", "updated apps (${apps.size} items)")
        }
    }

    fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP)
            LiveWallpaper.tap(v, event.rawX.toInt(), event.rawY.toInt())
        return false
    }

    private fun configureWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            window.setDecorFitsSystemWindows(false)
        else window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        feedRecycler.setPadding(0, getStatusBarHeight(), 0, 0)
        scrollBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = getNavigationBarHeight()
        }
    }
}