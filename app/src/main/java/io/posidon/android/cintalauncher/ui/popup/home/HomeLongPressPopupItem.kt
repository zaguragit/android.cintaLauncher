package io.posidon.android.cintalauncher.ui.popup.home

import android.graphics.drawable.Drawable
import android.view.View

class HomeLongPressPopupItem(
    val text: String,
    val description: String? = null,
    val icon: Drawable? = null,
    val isTitle: Boolean = false,
    val onClick: ((View) -> Unit)? = null,
)