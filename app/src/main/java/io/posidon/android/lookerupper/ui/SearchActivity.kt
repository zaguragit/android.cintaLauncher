package io.posidon.android.lookerupper.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.ui.color.ColorTheme
import io.posidon.android.lookerupper.data.Searcher
import io.posidon.android.lookerupper.data.providers.AppProvider
import io.posidon.android.lookerupper.data.providers.ContactProvider
import io.posidon.android.lookerupper.data.providers.DuckDuckGoProvider
import io.posidon.android.lookerupper.data.results.SearchResult

class SearchActivity : FragmentActivity() {

    val adapter = SearchAdapter()
    val searcher = Searcher(::AppProvider, ::ContactProvider, ::DuckDuckGoProvider, update = ::updateResults)

    private fun updateResults(list: List<SearchResult>) {
        runOnUiThread {
            adapter.update(list)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        searcher.onCreate(this)
        loadColors()
        val container = findViewById<View>(R.id.search_bar_container)!!
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
        }
        findViewById<TextView>(R.id.search_bar_text).doOnTextChanged { text, start, before, count ->
            searcher.query(text)
        }

    }

    private fun loadColors() {
        window.decorView.setBackgroundColor(ColorTheme.feedBG)
        findViewById<View>(R.id.search_bar_container).backgroundTintList =
            ColorStateList.valueOf(ColorTheme.searchBarBG)
        findViewById<TextView>(R.id.search_bar_text).run {
            setHintTextColor(ColorTheme.textColorForBG(context, ColorTheme.searchBarBG))
            setTextColor(ColorTheme.searchBarFG)
            highlightColor = ColorTheme.searchBarFG and 0x00ffffff or 0x88000000.toInt()
        }
        findViewById<ImageView>(R.id.search_bar_icon).imageTintList =
            ColorStateList.valueOf(ColorTheme.searchBarFG)
    }
}