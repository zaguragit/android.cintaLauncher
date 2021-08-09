package io.posidon.android.cintalauncher.ui.popup.drawer

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
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.storage.DoReshapeAdaptiveIconsSetting.doReshapeAdaptiveIcons
import io.posidon.android.cintalauncher.storage.ScrollbarControllerSetting.scrollbarController
import io.posidon.android.cintalauncher.storage.Settings
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.popup.PopupUtils
import io.posidon.android.cintalauncher.ui.popup.listPopup.ListPopupAdapter
import io.posidon.android.cintalauncher.ui.popup.listPopup.ListPopupItem
import io.posidon.android.cintalauncher.ui.settings.iconPackPicker.IconPackPickerActivity
import io.posidon.android.cintalauncher.ui.view.SeeThoughView
import posidon.android.conveniencelib.Device

object DrawerLongPressPopup {

    fun show(
        parent: View,
        touchX: Float,
        touchY: Float,
        navbarHeight: Int,
        settings: Settings,
        reloadScrollbarController: () -> Unit,
        reloadApps: () -> Unit,
    ) {
        val content = LayoutInflater.from(parent.context).inflate(R.layout.settings_popup, null)
        val window = PopupWindow(content, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)
        PopupUtils.setCurrent(window)

        content.findViewById<SeeThoughView>(R.id.blur_bg).run {
            drawable = acrylicBlur?.fullBlur?.let { BitmapDrawable(parent.resources, it) }
            alpha = 0.1f
        }

        val cardView = content.findViewById<CardView>(R.id.card)
        cardView.setCardBackgroundColor(ColorTheme.cardBG)
        content.findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = ListPopupAdapter().apply {
                fun update() {
                    updateItems(createMainAdapter(
                        parent.context,
                        settings,
                        reloadScrollbarController = {
                            cardView.post {
                                reloadScrollbarController()
                                update()
                            }
                        },
                        reloadApps = reloadApps,
                    ))
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
        reloadScrollbarController: () -> Unit,
        reloadApps: () -> Unit,
    ): List<ListPopupItem> {
        return listOf(
            //ListPopupItem(context.getString(R.string.drawer_settings), isTitle = true),
            ListPopupItem(
                context.getString(R.string.scrollbar_controller),
                description = context.resources.getStringArray(R.array.scrollbar_controllers)[settings.scrollbarController],
                icon = ContextCompat.getDrawable(context, R.drawable.ic_sorting),
            ) {
                AlertDialog.Builder(context)
                    .setSingleChoiceItems(R.array.scrollbar_controllers, settings.scrollbarController) { d, i ->
                        settings.edit(context) {
                            scrollbarController = context.resources.getStringArray(R.array.scrollbar_controllers_data)[i].toInt()
                            reloadScrollbarController()
                        }
                        d.dismiss()
                    }
                    .show()
            },
            ListPopupItem(context.getString(R.string.icons), isTitle = true),
            ListPopupItem(
                context.getString(R.string.icon_packs),
                icon = ContextCompat.getDrawable(context, R.drawable.ic_shapes),
            ) {
                context.startActivity(Intent(context, IconPackPickerActivity::class.java))
            },
            ListPopupItem(
                context.getString(R.string.reshape_adaptive_icons),
                description = context.getString(R.string.reshape_adaptive_icons_explanation),
                icon = ContextCompat.getDrawable(context, R.drawable.ic_shapes),
                value = settings.doReshapeAdaptiveIcons,
                onToggle = { v, value ->
                    settings.edit(context) {
                        doReshapeAdaptiveIcons = value
                        reloadApps()
                    }
                }
            ),
        )
    }
}