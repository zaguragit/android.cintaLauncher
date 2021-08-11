package io.posidon.android.cintalauncher.ui

import android.Manifest
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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.graphics.toRectF
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.color.ColorThemeOptions
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.providers.AppSuggestionsManager
import io.posidon.android.cintalauncher.providers.Feed
import io.posidon.android.cintalauncher.providers.app.AppCallback
import io.posidon.android.cintalauncher.providers.app.AppCollection
import io.posidon.android.cintalauncher.providers.notification.NotificationProvider
import io.posidon.android.cintalauncher.providers.rss.RssProvider
import io.posidon.android.cintalauncher.storage.*
import io.posidon.android.cintalauncher.storage.ColorThemeDayNightSetting.colorThemeDayNight
import io.posidon.android.cintalauncher.storage.ColorThemeSetting.colorTheme
import io.posidon.android.cintalauncher.storage.ScrollbarControllerSetting.SCROLLBAR_CONTROLLER_BY_HUE
import io.posidon.android.cintalauncher.storage.ScrollbarControllerSetting.scrollbarController
import io.posidon.android.cintalauncher.ui.drawer.AppDrawer
import io.posidon.android.cintalauncher.ui.feed.items.FeedAdapter
import io.posidon.android.cintalauncher.ui.popup.PopupUtils
import io.posidon.android.cintalauncher.ui.view.scrollbar.Scrollbar
import io.posidon.android.cintalauncher.ui.view.scrollbar.alphabet.AlphabetScrollbarController
import io.posidon.android.cintalauncher.ui.view.scrollbar.hue.HueScrollbarController
import io.posidon.android.cintalauncher.util.InvertedRoundRectDrawable
import io.posidon.android.cintalauncher.util.StackTraceActivity
import io.posidon.android.cintalauncher.util.blur.AcrylicBlur
import io.posidon.android.launcherutils.AppLoader
import io.posidon.android.launcherutils.GestureNavContract
import io.posidon.android.launcherutils.LiveWallpaper
import posidon.android.conveniencelib.*
import kotlin.concurrent.thread

var acrylicBlur: AcrylicBlur? = null
    private set

class LauncherActivity : FragmentActivity() {

    val feed = Feed()
    val settings = Settings()

    val suggestionsManager = AppSuggestionsManager()

    val notificationProvider = NotificationProvider(this)

    val feedRecycler by lazy { findViewById<RecyclerView>(R.id.feed_recycler)!! }
    val scrollBar by lazy { findViewById<Scrollbar>(R.id.scroll_bar)!! }

    val scrollBarContainer by lazy { findViewById<View>(R.id.scroll_bar_container)!! }

    val blurBG by lazy { findViewById<View>(R.id.blur_bg)!! }

    val appDrawer by lazy { AppDrawer(this, scrollBar) }

    lateinit var feedAdapter: FeedAdapter

    lateinit var wallpaperManager: WallpaperManager

    val appLoader = AppLoader({ packageName, name, profile, label, icon -> App(packageName, name, profile, label, icon, settings) }, ::AppCollection)

    var blurBitmap: Bitmap? = null

    var colorThemeOptions = ColorThemeOptions(settings.colorThemeDayNight)

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
        feedRecycler.setOnScrollChangeListener { _, _, scrollY, _, _ -> feedAdapter.onScroll(scrollY) }

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

    private fun loadBlur() = thread(isDaemon = true, name = "Blur thread") {
        if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED) {
            if (blurBitmap == null) return@thread
            blurBitmap = null
            runOnUiThread(::updateBlur)
            return@thread
        }
        AcrylicBlur.blurWallpaper(this, wallpaperManager.drawable) {
            acrylicBlur = it
            blurBitmap = it.fullBlur
            runOnUiThread(::updateBlur)
        }
    }

    fun updateBlur() {
        val b = acrylicBlur
        val r = resources.getDimension(R.dimen.dock_corner_radius)
        if (b == null) {
            blurBG.background = null
            scrollBarContainer.background = ColorDrawable(ColorTheme.scrollBarDefaultBG)
            findViewById<View>(R.id.home_container).foreground = InvertedRoundRectDrawable(
                floatArrayOf(0f, 0f, 0f, 0f, r, r, r, r), 0f, ColorTheme.scrollBarDefaultBG)
            return
        }
        blurBG.background = LayerDrawable(arrayOf(
            BitmapDrawable(resources, b.partialBlurSmall),
            BitmapDrawable(resources, b.partialBlurMedium),
            BitmapDrawable(resources, b.fullBlur),
        )).apply {
            setLayerInsetBottom(0, -scrollBarContainer.measuredHeight)
            setLayerInsetBottom(1, -scrollBarContainer.measuredHeight)
            setLayerInsetBottom(2, -scrollBarContainer.measuredHeight)
        }
        scrollBarContainer.background = LayerDrawable(arrayOf(
            BitmapDrawable(resources, b.fullBlur),
            ColorDrawable(ColorTheme.scrollBarTintBG)
        )).apply { setLayerInsetTop(0, scrollBarContainer.measuredHeight - Device.screenHeight(this@LauncherActivity)) }
        findViewById<View>(R.id.home_container).foreground = LayerDrawable(arrayOf(
            InvertedRoundRectDrawable(
                floatArrayOf(0f, 0f, 0f, 0f, r, r, r, r),
                0f,
                0xff000000.toInt()
            ).apply {
                outerPaint.shader = BitmapShader(b.fullBlur, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            },
            InvertedRoundRectDrawable(
                floatArrayOf(0f, 0f, 0f, 0f, r, r, r, r),
                0f,
                ColorTheme.scrollBarTintBG
            )
        ))
        feedAdapter.onScroll(feedRecycler.scrollY)
    }

    private fun updateColorTheme() {
        feedAdapter.updateColorTheme()
        appDrawer.updateColorTheme()
        scrollBar.controller.updateTheme(this)
        updateBlur()
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
            if (blurBitmap == null) {
                loadBlur()
            }
            if (shouldUpdate) {
                thread(name = "Reloading color theme", isDaemon = true, block = this::reloadColorThemeSync)
            }
        }
        val current = System.currentTimeMillis()
        if (current - lastUpdateTime > 1000L * 60L * 5L) {
            lastUpdateTime = current
            thread (isDaemon = true, block = RssProvider::update)
        }
        if (shouldUpdate) {
            reloadScrollbarController()
        } else {
            suggestionsManager.onResume(this)
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

    fun reloadScrollbarController() {
        updateScrollbarController()
        loadApps()
    }

    private fun updateScrollbarController() {
        when (settings.scrollbarController) {
            SCROLLBAR_CONTROLLER_BY_HUE -> {
                if (scrollBar.controller !is HueScrollbarController) {
                    scrollBar.controller = HueScrollbarController(scrollBar)
                }
            }
            else -> {
                if (scrollBar.controller !is AlphabetScrollbarController) {
                    scrollBar.controller = AlphabetScrollbarController(scrollBar)
                }
            }
        }
        scrollBar.controller.updateTheme(this)
    }

    override fun onPause() {
        super.onPause()
        if (appDrawer.isOpen) {
            appDrawer.close()
        }
        PopupUtils.dismissCurrent()
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
        loadBlur()
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
            scrollBar.controller.loadSections(apps)
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