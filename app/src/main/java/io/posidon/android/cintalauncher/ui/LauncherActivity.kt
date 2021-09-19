package io.posidon.android.cintalauncher.ui

import android.Manifest
import android.app.Activity
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.LauncherContext
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.color.ColorThemeOptions
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.data.feed.items.isToday
import io.posidon.android.cintalauncher.providers.app.AppCallback
import io.posidon.android.cintalauncher.providers.app.AppCollection
import io.posidon.android.cintalauncher.providers.feed.FeedFilter
import io.posidon.android.cintalauncher.providers.feed.notification.NotificationProvider
import io.posidon.android.cintalauncher.providers.feed.rss.RssProvider
import io.posidon.android.cintalauncher.providers.suggestions.SuggestionsManager
import io.posidon.android.cintalauncher.storage.*
import io.posidon.android.cintalauncher.storage.ColorThemeDayNightSetting.colorThemeDayNight
import io.posidon.android.cintalauncher.storage.ColorThemeSetting.colorTheme
import io.posidon.android.cintalauncher.ui.drawer.AppDrawer
import io.posidon.android.cintalauncher.ui.feed.FeedAdapter
import io.posidon.android.cintalauncher.ui.feed.filters.FeedFilterAdapter
import io.posidon.android.cintalauncher.ui.feed.home.HomeViewHolder
import io.posidon.android.cintalauncher.ui.popup.PopupUtils
import io.posidon.android.cintalauncher.ui.view.scrollbar.Scrollbar
import io.posidon.android.cintalauncher.ui.view.scrollbar.ScrollbarIconView
import io.posidon.android.cintalauncher.util.StackTraceActivity
import io.posidon.android.cintalauncher.util.blur.AcrylicBlur
import io.posidon.android.launcherutils.LiveWallpaper
import posidon.android.conveniencelib.*
import kotlin.concurrent.thread

var acrylicBlur: AcrylicBlur? = null
    private set

class LauncherActivity : FragmentActivity() {

    val launcherContext = LauncherContext()

    val settings by launcherContext::settings

    val notificationProvider = NotificationProvider(this)

    val homeContainer by lazy { findViewById<View>(R.id.home_container) }
    val feedRecycler by lazy { findViewById<RecyclerView>(R.id.feed_recycler)!! }
    val feedFilterRecycler by lazy { findViewById<RecyclerView>(R.id.feed_filters_recycler)!! }

    val blurBG by lazy { findViewById<View>(R.id.blur_bg)!! }

    val appDrawer by lazy { AppDrawer(this) }

    lateinit var feedAdapter: FeedAdapter
    lateinit var feedFilterAdapter: FeedFilterAdapter

    private lateinit var wallpaperManager: WallpaperManager

    var colorThemeOptions = ColorThemeOptions(settings.colorThemeDayNight)

    val scrollBar: Scrollbar get() = appDrawer.scrollIcon.scrollBar
    val scrollBarIcon: ScrollbarIconView get() = appDrawer.scrollIcon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StackTraceActivity.init(applicationContext)
        setContentView(R.layout.activity_launcher)
        configureWindow()
        settings.init(applicationContext)
        colorThemeOptions = ColorThemeOptions(settings.colorThemeDayNight)
        ColorTheme.onCreate(colorThemeOptions, this)
        wallpaperManager = WallpaperManager.getInstance(this)

        feedRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        feedAdapter = FeedAdapter(this)
        feedAdapter.setHasStableIds(true)
        feedRecycler.setItemViewCacheSize(20)
        feedRecycler.adapter = feedAdapter
        feedRecycler.addOnScrollListener(feedScrollListener)

        feedFilterAdapter = FeedFilterAdapter(launcherContext)
        feedFilterRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        feedFilterRecycler.adapter = feedFilterAdapter

        feedFilterAdapter.updateItems(
            FeedFilter(getString(R.string.today), filter = FeedItem::isToday),
            FeedFilter(getString(R.string.news)) {
                it.meta?.isNotification != true
            },
            FeedFilter(getString(R.string.notifications)) {
                it.meta?.isNotification == true
            },
        )

        launcherContext.feed.init(settings, notificationProvider, RssProvider, onUpdate = this::loadFeed) {
            runOnUiThread {
                feedAdapter.onFeedInitialized()
            }
        }

        homeContainer.setOnDragListener(::onDrag)

        appDrawer.init()

        window.decorView.findViewById<View>(android.R.id.content).setOnTouchListener(::onTouch)

        val launcherApps = getSystemService(LauncherApps::class.java)
        launcherApps.registerCallback(AppCallback(::loadApps))

        loadApps()
        updateColorTheme()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            wallpaperManager.addOnColorsChangedListener(::onColorsChangedListener, feedRecycler.handler)
            thread(name = "onCreate color update", isDaemon = true) {
                ColorTheme.onColorsChanged(this, settings.colorTheme, colorThemeOptions, LauncherActivity::updateColorTheme) {
                    wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                }
            }
            onWallpaperChanged()
        }
    }

    fun onDrag(v: View, event: DragEvent): Boolean {
        val viewUnder = feedRecycler.findChildViewUnder(event.x, event.y)
        if (viewUnder != null && feedRecycler.findContainingViewHolder(viewUnder) is HomeViewHolder) {
            println("drag: " + event.action)
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED,
                DragEvent.ACTION_DRAG_ENTERED,
                DragEvent.ACTION_DRAG_LOCATION -> {
                    val i = feedAdapter.getPinnedItemIndex(event.x, event.y)
                    val pinnedItems = launcherContext.appManager.pinnedItems
                    feedAdapter.showDropTarget(if (i == -1) pinnedItems.size else i)
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    feedAdapter.updatePinned()
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    feedAdapter.showDropTarget(-1)
                }
                DragEvent.ACTION_DROP -> {
                    val i = feedAdapter.getPinnedItemIndex(event.x, event.y)
                    if (i == -1)
                        return false
                    feedAdapter.onDrop(v, i, event.clipData)
                }
            }
        } else {
            feedAdapter.showDropTarget(-1)
        }
        return true
    }

    val feedScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                feedAdapter.onScroll(0)
                showFilters(recyclerView.canScrollVertically(-1))
            }
        }

        var lastScrollCheck = false
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val scrollCheck = recyclerView.canScrollVertically(-1)
            feedAdapter.onScroll(0)
            if (scrollCheck != lastScrollCheck) {
                showFilters(scrollCheck)
            }
            lastScrollCheck = scrollCheck
        }

        fun showFilters(show: Boolean) {
            if (show) {
                feedFilterRecycler.isVisible = true
                feedFilterRecycler.animate().apply {
                    interpolator = DecelerateInterpolator()
                    translationY(0f)
                    duration = 100L
                    startDelay = 50L
                    onEnd { feedFilterRecycler.isVisible = true }
                }
            } else {
                feedFilterRecycler.animate().apply {
                    interpolator = AccelerateInterpolator()
                    translationY(feedFilterRecycler.height.toFloat())
                    duration = 100L
                    startDelay = 0L
                    onEnd { feedFilterRecycler.isVisible = false }
                }
            }
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
        SuggestionsManager.onResume(this)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            thread (isDaemon = true) {
                ColorTheme.onResumePreOMR1(
                    this,
                    settings.colorTheme,
                    colorThemeOptions,
                    LauncherActivity::updateColorTheme
                )
                onWallpaperChanged()
            }
        } else {
            if (acrylicBlur == null) {
                loadBlur(wallpaperManager, ::updateBlur)
            }
        }
        val current = System.currentTimeMillis()
        if (shouldUpdate || current - lastUpdateTime > 1000L * 60L * 5L) {
            lastUpdateTime = current
            thread (isDaemon = true, block = RssProvider::update)
        }
    }

    override fun onPause() {
        super.onPause()
        if (appDrawer.isOpen) {
            appDrawer.close()
        }
        PopupUtils.dismissCurrent()
        SuggestionsManager.onPause(settings, this)
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

    fun updateBlur() {
        val b = acrylicBlur
        val r = resources.getDimension(R.dimen.dock_corner_radius)
        if (b == null) {
            blurBG.background = null
            return
        }
        blurBG.background = LayerDrawable(arrayOf(
            BitmapDrawable(resources, b.partialBlurSmall),
            BitmapDrawable(resources, b.partialBlurMedium),
            BitmapDrawable(resources, b.fullBlur),
            BitmapDrawable(resources, b.insaneBlur).also {
                it.alpha = 160
            },
        ))
        feedAdapter.onScroll(feedRecycler.scrollY)
    }

    private fun updateColorTheme() {
        feedAdapter.updateColorTheme()
        feedFilterAdapter.updateColorTheme()
        updateBlur()
        appDrawer.updateColorTheme()
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    fun onColorsChangedListener(
        colors: WallpaperColors?,
        which: Int
    ) {
        if (which and WallpaperManager.FLAG_SYSTEM != 0) {
            onWallpaperChanged()
            ColorTheme.onColorsChanged(this, settings.colorTheme, colorThemeOptions, LauncherActivity::updateColorTheme) { colors }
        }
    }

    fun reloadColorThemeSync() {
        colorThemeOptions = ColorThemeOptions(settings.colorThemeDayNight)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            ColorTheme.onColorsChanged(this, settings.colorTheme, colorThemeOptions, LauncherActivity::updateColorTheme) {
                wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            }
        } else ColorTheme.onResumePreOMR1(this, settings.colorTheme, colorThemeOptions, LauncherActivity::updateColorTheme)
    }

    private fun onWallpaperChanged() {
        loadBlur(wallpaperManager, ::updateBlur)
    }

    private fun handleGestureContract(intent: Intent) {
        //val gnc = GestureNavContract.fromIntent(intent)
        //gnc?.sendEndPosition(scrollBar.clipBounds.toRectF(), null)
    }

    fun loadFeed(items: List<FeedItem>) {
        runOnUiThread {
            feedAdapter.updateItems(items)
            Log.d("Cinta", "updated feed (${items.size} items)")
        }
    }

    fun loadApps() {
        launcherContext.appManager.loadApps(this) { apps: AppCollection ->
            scrollBarIcon.reloadController(settings)
            scrollBar.controller.loadSections(apps)
            appDrawer.update(scrollBar, apps.sections)
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

        feedRecycler.setPadding(0, 0, 0, getNavigationBarHeight())
        feedFilterRecycler.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = getNavigationBarHeight()
        }
    }

    companion object {
        fun Activity.loadBlur(wallpaperManager: WallpaperManager, updateBlur: () -> Unit) = thread(isDaemon = true, name = "Blur thread") {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                if (acrylicBlur == null) return@thread
                acrylicBlur = null
                runOnUiThread(updateBlur)
                return@thread
            }
            val drawable = wallpaperManager.peekDrawable()
            if (drawable == null) {
                if (acrylicBlur == null) return@thread
                acrylicBlur = null
                runOnUiThread(updateBlur)
                return@thread
            }
            AcrylicBlur.blurWallpaper(this, drawable) {
                acrylicBlur = it
                runOnUiThread(updateBlur)
            }
        }
    }
}