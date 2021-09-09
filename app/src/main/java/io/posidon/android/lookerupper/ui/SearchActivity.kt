package io.posidon.android.lookerupper.ui

import android.app.SearchManager
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.color.ColorThemeOptions
import io.posidon.android.cintalauncher.storage.ColorThemeDayNightSetting.colorThemeDayNight
import io.posidon.android.cintalauncher.storage.ColorThemeSetting.colorTheme
import io.posidon.android.cintalauncher.storage.Settings
import io.posidon.android.cintalauncher.ui.LauncherActivity.Companion.loadBlur
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.view.SeeThoughView
import io.posidon.android.lookerupper.data.Searcher
import io.posidon.android.lookerupper.data.providers.AppProvider
import io.posidon.android.lookerupper.data.providers.ContactProvider
import io.posidon.android.lookerupper.data.providers.DuckDuckGoProvider
import io.posidon.android.lookerupper.data.results.SearchResult

class SearchActivity : FragmentActivity() {

    lateinit var adapter: SearchAdapter
    val settings = Settings()
    val searcher = Searcher(
        settings,
        ::AppProvider,
        ::ContactProvider,
        ::DuckDuckGoProvider,
        update = ::updateResults
    )

    private fun updateResults(list: List<SearchResult>) = runOnUiThread {
        adapter.update(list)
    }

    val container by lazy { findViewById<View>(R.id.search_bar_container)!! }
    val searchBar by lazy { findViewById<View>(R.id.search_bar)!! }
    val blurBG by lazy { findViewById<SeeThoughView>(R.id.blur_bg)!! }

    val colorThemeOptions by lazy { ColorThemeOptions(settings.colorThemeDayNight) }
    val wallpaperManager by lazy { getSystemService(WallpaperManager::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        settings.init(this)
        searcher.onCreate(this)
        ColorTheme.onCreate(colorThemeOptions, this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            wallpaperManager.addOnColorsChangedListener(::onColorsChangedListener, container.handler)
        }
        loadColors()
        val recyclerView = findViewById<RecyclerView>(R.id.recycler)!!
        adapter = SearchAdapter(this, recyclerView, false)
        recyclerView.run {
            layoutManager = GridLayoutManager(this@SearchActivity, 3, RecyclerView.VERTICAL, false).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(i: Int): Int =
                        if (adapter?.getItemViewType(i) == SearchAdapter.RESULT_APP) 1
                        else 3
                }
            }
            this.adapter = this@SearchActivity.adapter
            container.post {
                setPadding(paddingLeft, container.measuredHeight, paddingRight, paddingBottom)
            }
            setOnScrollChangeListener { _, _, _, _, _ ->
                this@SearchActivity.adapter.onScroll()
            }
        }
        findViewById<EditText>(R.id.search_bar_text).run {
            doOnTextChanged { text, start, before, count ->
                searcher.query(text)
            }
            setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val viewSearch = Intent(Intent.ACTION_WEB_SEARCH)
                    viewSearch.putExtra(SearchManager.QUERY, v.text)
                    v.context.startActivity(viewSearch)
                    true
                } else false
            }
        }
    }

    fun updateBlur() {
        blurBG.drawable = BitmapDrawable(resources, acrylicBlur?.smoothBlur)
        window.decorView.background = LayerDrawable(arrayOf(
            BitmapDrawable(resources, acrylicBlur?.partialBlurSmall),
            BitmapDrawable(resources, acrylicBlur?.insaneBlur).also {
                it.alpha = 80
            },
            ColorDrawable(ColorTheme.uiBG).also {
                it.alpha = 120
            },
        ))
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    fun onColorsChangedListener(
        colors: WallpaperColors?,
        which: Int
    ) {
        if (which and WallpaperManager.FLAG_SYSTEM != 0) {
            loadBlur(wallpaperManager, ::updateBlur)
            ColorTheme.onColorsChanged(this, settings.colorTheme, colorThemeOptions, SearchActivity::loadColors) { colors }
        }
    }

    private fun loadColors() {
        window.decorView.background = LayerDrawable(arrayOf(
            BitmapDrawable(resources, acrylicBlur?.partialBlurSmall),
            BitmapDrawable(resources, acrylicBlur?.insaneBlur).also {
                it.alpha = 80
            },
            ColorDrawable(ColorTheme.uiBG).also {
                it.alpha = 120
            },
        ))
        searchBar.backgroundTintList =
            ColorStateList.valueOf(ColorTheme.searchBarBG)
        searchBar.findViewById<TextView>(R.id.search_bar_text).run {
            setTextColor(ColorTheme.searchBarFG)
            highlightColor = ColorTheme.searchBarFG and 0x00ffffff or 0x66000000
        }
        searchBar.findViewById<ImageView>(R.id.search_bar_icon).imageTintList =
            ColorStateList.valueOf(ColorTheme.searchBarFG)
    }
}