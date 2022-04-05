package io.posidon.android.cintalauncher.ui.popup.home

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
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
import io.posidon.android.cintalauncher.providers.color.theme.ColorTheme
import io.posidon.android.cintalauncher.storage.ColorThemeDayNightSetting.colorThemeDayNight
import io.posidon.android.cintalauncher.storage.ColorThemeDayNightSetting.setColorThemeDayNight
import io.posidon.android.cintalauncher.storage.ColorExtractorSetting.colorTheme
import io.posidon.android.cintalauncher.storage.Settings
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.popup.PopupUtils
import io.posidon.android.cintalauncher.ui.popup.listPopup.ListPopupAdapter
import io.posidon.android.cintalauncher.ui.popup.listPopup.ListPopupItem
import io.posidon.android.cintalauncher.ui.settings.feedChooser.FeedSourcesChooserActivity
import io.posidon.android.cintalauncher.ui.settings.iconPackPicker.IconPackPickerActivity
import io.posidon.android.cintalauncher.ui.view.SeeThoughView
import io.posidon.android.conveniencelib.Device
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

object HomeLongPressPopup {

    fun show(
        parent: View,
        touchX: Float,
        touchY: Float,
        navbarHeight: Int,
        settings: Settings,
        reloadColorTheme: () -> Unit,
    ) {
        val content = LayoutInflater.from(parent.context).inflate(R.layout.list_popup, null)
        val window = PopupWindow(content, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)
        PopupUtils.setCurrent(window)

        content.findViewById<SeeThoughView>(R.id.blur_bg).run {
            drawable = acrylicBlur?.fullBlur?.let { BitmapDrawable(parent.resources, it) }
        }

        val cardView = content.findViewById<CardView>(R.id.card)
        cardView.setCardBackgroundColor(ColorTheme.cardBG)
        content.findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = ListPopupAdapter().apply {
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

        val gravity = Gravity.CENTER
        val x = touchX.toInt() - Device.screenWidth(parent.context) / 2
        val y = touchY.toInt() - Device.screenHeight(parent.context) / 2 + navbarHeight
        window.showAtLocation(parent, gravity, x, y)
    }

    private fun createMainAdapter(
        context: Context,
        settings: Settings,
        reloadColorTheme: () -> Unit,
    ): List<ListPopupItem> {
        return listOf(
            ListPopupItem(
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
            ListPopupItem(
                context.getString(R.string.color_theme_day_night),
                description = context.resources.getStringArray(R.array.color_theme_day_night)[settings.colorThemeDayNight.ordinal],
                icon = ContextCompat.getDrawable(context, R.drawable.ic_lightness),
            ) {
                AlertDialog.Builder(context)
                    .setSingleChoiceItems(R.array.color_theme_day_night, settings.colorThemeDayNight.ordinal) { d, i ->
                        settings.edit(context) {
                            setColorThemeDayNight(context.resources.getStringArray(R.array.color_theme_day_night_data)[i].toInt())
                            reloadColorTheme()
                        }
                        d.dismiss()
                    }
                    .show()
            },
            ListPopupItem(
                context.getString(R.string.rss_sources),
                icon = ContextCompat.getDrawable(context, R.drawable.ic_news),
            ) {
                context.startActivity(Intent(context, FeedSourcesChooserActivity::class.java))
            },
            ListPopupItem(
                context.getString(R.string.icon_packs),
                icon = ContextCompat.getDrawable(context, R.drawable.ic_shapes),
            ) {
                context.startActivity(Intent(context, IconPackPickerActivity::class.java))
            },
        )
    }
}