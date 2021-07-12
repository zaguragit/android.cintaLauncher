package io.posidon.android.lookerupper.data.results

import android.graphics.drawable.Drawable

abstract class SimpleResult(
    override val title: String,
    val icon: Drawable,
    val subtitle: String? = null
) : SearchResult