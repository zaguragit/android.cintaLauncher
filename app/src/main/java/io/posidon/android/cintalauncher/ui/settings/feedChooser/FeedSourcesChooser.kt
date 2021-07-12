package io.posidon.android.cintalauncher.ui.settings.feedChooser

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.storage.Settings
import io.posidon.android.cintalauncher.ui.color.ColorTheme
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.getNavigationBarHeight
import posidon.android.conveniencelib.getStatusBarHeight
import posidon.android.conveniencelib.vibrate

class FeedSourcesChooser : AppCompatActivity() {

    val settings = Settings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feed_chooser)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        settings.init(applicationContext)
        loadColors()

        val grid = findViewById<RecyclerView>(R.id.recycler)
        grid.layoutManager = GridLayoutManager(this, 2)
        val padding = dp(4).toInt()
        grid.setPadding(padding, getStatusBarHeight(), padding, getNavigationBarHeight() + padding)

        val feedUrls = settings.getStrings("feed:rss_sources")?.let { arrayListOf(*it) } ?: arrayListOf()

        grid.adapter = FeedChooserAdapter(settings, feedUrls)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.backgroundTintList = ColorStateList.valueOf(ColorTheme.accentColor and 0x00ffffff or 0x33000000)
        fab.imageTintList = ColorStateList.valueOf(ColorTheme.accentColor)
        fab.setOnClickListener {
            sourceEditPopup(it.context, settings, feedUrls, grid.adapter!!)
        }
        (fab.layoutParams as FrameLayout.LayoutParams).bottomMargin = dp(20).toInt() + getNavigationBarHeight()
    }

    private fun loadColors() {
        window.decorView.setBackgroundColor(ColorTheme.feedBG)
    }

    companion object {
        inline fun sourceEditPopup(
            context: Context,
            settings: Settings,
            feedUrls: ArrayList<String>,
            adapter: RecyclerView.Adapter<*>,
            i: Int = -1
        ) {
            context.vibrate(14)
            val dialog = BottomSheetDialog(context, R.style.Theme_CintaLauncher_BottomSheetPopup)
            dialog.setContentView(R.layout.feed_chooser_option_edit_dialog)
            dialog.window!!.findViewById<View>(R.id.design_bottom_sheet).run {
                setBackgroundResource(R.drawable.bottom_sheet)
                backgroundTintList = ColorStateList.valueOf(ColorTheme.appDrawerColor)
            }

            val textColor = ColorTheme.textColorForBG(context, ColorTheme.appDrawerColor)
            val hintColor = ColorTheme.hintColorForBG(context, ColorTheme.appDrawerColor)
            val input = dialog.findViewById<EditText>(R.id.title)!!
            input.setTextColor(textColor)
            input.setHintTextColor(hintColor)

            dialog.findViewById<TextView>(R.id.done)!!.run {
                setTextColor(ColorTheme.titleColorForBG(context, ColorTheme.appDrawerButtonColor))
                backgroundTintList = ColorStateList.valueOf(ColorTheme.appDrawerButtonColor)
                setOnClickListener {
                    dialog.dismiss()

                    if (i == -1) feedUrls.add(input.text.toString())
                    else feedUrls[i] = input.text.toString()

                    adapter.notifyDataSetChanged()
                    settings.edit(context) {
                        "feed:rss_sources" set feedUrls.toTypedArray()
                    }
                }
            }

            if (i == -1) {
                val suggestions = Suggestions(context)
                dialog.findViewById<ExpandableListView>(R.id.list)!!.apply {
                    visibility = View.VISIBLE
                    setAdapter(SuggestionsAdapter(suggestions))
                    setOnChildClickListener { _, _, topicI, sourceI, _ ->
                        input.text.run {
                            clear()
                            insert(0, suggestions[topicI][sourceI].url)
                        }
                        true
                    }
                }
                dialog.findViewById<TextView>(R.id.remove)!!.visibility = View.GONE
            } else {
                val url = feedUrls[i]
                dialog.findViewById<TextView>(R.id.remove)!!.run {
                    val hsl = FloatArray(3)
                    ColorUtils.colorToHSL(ColorTheme.appDrawerButtonColor, hsl)
                    hsl[0] = 0f
                    hsl[1] = hsl[1].coerceAtLeast(0.3f)
                    val bg = ColorUtils.HSLToColor(hsl)
                    backgroundTintList = ColorStateList.valueOf(bg)
                    setTextColor(ColorTheme.titleColorForBG(context, bg))
                    setOnClickListener {
                        dialog.dismiss()
                        feedUrls.remove(url)
                        adapter.notifyDataSetChanged()
                        settings.edit(it.context) {
                            "feed:rss_sources" set feedUrls.toTypedArray()
                        }
                    }
                }
                input.text = Editable.Factory().newEditable(url)
            }

            dialog.show()
        }
    }
}