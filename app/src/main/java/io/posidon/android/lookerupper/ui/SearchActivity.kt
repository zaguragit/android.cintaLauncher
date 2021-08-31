package io.posidon.android.lookerupper.ui

import android.app.SearchManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.storage.Settings
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.view.SeeThoughView
import io.posidon.android.lookerupper.data.Searcher
import io.posidon.android.lookerupper.data.providers.AppProvider
import io.posidon.android.lookerupper.data.providers.ContactProvider
import io.posidon.android.lookerupper.data.providers.DuckDuckGoProvider
import io.posidon.android.lookerupper.data.results.SearchResult
import posidon.android.conveniencelib.getNavigationBarHeight


class SearchActivity : FragmentActivity() {

    lateinit var adapter: SearchAdapter
    val settings = Settings()
    val searcher = Searcher(settings, ::AppProvider, ::ContactProvider, ::DuckDuckGoProvider, update = ::updateResults)

    private fun updateResults(list: List<SearchResult>) = runOnUiThread {
        adapter.update(list)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        settings.init(this)
        searcher.onCreate(this)
        loadColors()
        val container = findViewById<View>(R.id.search_bar_container)!!
        adapter = SearchAdapter(this, getNavigationBarHeight())
        findViewById<RecyclerView>(R.id.recycler).apply {
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
        val blurBG = findViewById<SeeThoughView>(R.id.blur_bg)!!
        blurBG.drawable = BitmapDrawable(resources, acrylicBlur?.smoothBlur)
    }

    private fun loadColors() {
        window.decorView.background = LayerDrawable(arrayOf(
            BitmapDrawable(resources, acrylicBlur?.partialBlurSmall),
            ColorDrawable(ColorTheme.uiBG),
        ))
        findViewById<View>(R.id.search_bar_container).backgroundTintList =
            ColorStateList.valueOf(ColorTheme.searchBarBG)
        findViewById<TextView>(R.id.search_bar_text).run {
            setTextColor(ColorTheme.searchBarFG)
            highlightColor = ColorTheme.searchBarFG and 0x00ffffff or 0x66000000
        }
        findViewById<ImageView>(R.id.search_bar_icon).imageTintList =
            ColorStateList.valueOf(ColorTheme.searchBarFG)
    }
}