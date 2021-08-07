package io.posidon.android.cintalauncher.ui.popup.home

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.ListPopupWindow
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.storage.ColorThemeSetting.colorTheme
import io.posidon.android.cintalauncher.storage.Settings
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.popup.PopupUtils
import io.posidon.android.cintalauncher.ui.settings.feedChooser.FeedSourcesChooserActivity
import io.posidon.android.cintalauncher.ui.view.SeeThoughView
import posidon.android.conveniencelib.dp
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

object HomeLongPressPopup {
    private var currentPopup: PopupWindow? = null

    fun show(
        parent: View,
        touchX: Float,
        touchY: Float,
        navbarHeight: Int,
        settings: Settings,
        reloadColorTheme: () -> Unit,
    ) {
        val content = LayoutInflater.from(parent.context).inflate(R.layout.long_press_home_popup, null)
        val window = PopupWindow(content, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)
        currentPopup = window
        window.setOnDismissListener {
            currentPopup = null
        }

        content.findViewById<SeeThoughView>(R.id.blur_bg).run {
            drawable = acrylicBlur?.fullBlur?.let { BitmapDrawable(parent.resources, it) }
        }

        val cardView = content.findViewById<CardView>(R.id.card)
        cardView.setCardBackgroundColor(ColorTheme.cardBG)
        content.findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = HomeLongPressPopupAdapter().apply {
                val updateLock = ReentrantLock()
                fun update() {
                    updateItems(createMainAdapter(parent.context, settings) {
                        thread(name = "Reloading color theme", isDaemon = true) {
                            updateLock.withLock {
                                reloadColorTheme()
                                cardView.post {
                                    cardView.setCardBackgroundColor(ColorTheme.cardBG)
                                    update()
                                }
                            }
                        }
                    })
                }
                update()
            }
        }

        val (x, y, gravity) = PopupUtils.getPopupLocation(parent.context, touchX.toInt(), touchY.toInt(), 0, 0, navbarHeight, -parent.dp(64).toInt(), 0)
        window.showAtLocation(parent, gravity, x, y)
    }

    private fun createMainAdapter(
        context: Context,
        settings: Settings,
        reloadColorTheme: () -> Unit,
    ): List<HomeLongPressPopupItem> {
        return listOf(
            HomeLongPressPopupItem(context.getString(R.string.launcher_settings), isTitle = true),
            HomeLongPressPopupItem(
                context.getString(R.string.color_theme_gen),
                description = context.resources.getStringArray(R.array.color_theme_gens)[settings.colorTheme],
                icon = ContextCompat.getDrawable(context, R.drawable.ic_color_dropper),
            ) {
                AlertDialog.Builder(context)
                    .setSingleChoiceItems(R.array.color_theme_gens, settings.colorTheme) { d, i ->
                        settings.edit(context) {
                            colorTheme = context.resources.getStringArray(R.array.color_theme_gens_data)[i].toInt()
                            reloadColorTheme()
                        }
                        d.dismiss()
                    }
                    .show()
            },
            HomeLongPressPopupItem(
                context.getString(R.string.rss_sources),
                icon = ContextCompat.getDrawable(context, R.drawable.ic_news),
            ) {
                context.startActivity(Intent(context, FeedSourcesChooserActivity::class.java))
            },
        )
    }

    fun dismiss() {
        currentPopup?.dismiss()
    }
}