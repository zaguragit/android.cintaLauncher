package io.posidon.android.cintalauncher.ui.feed.home.summary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.feed.summary.SummaryItem
import io.posidon.android.cintalauncher.data.feed.summary.media.MediaSummary
import io.posidon.android.cintalauncher.data.feed.summary.notification.NotificationSummary

class SummaryAdapter : RecyclerView.Adapter<SummaryViewHolder>() {

    var summaries: List<SummaryItem> = emptyList()

    override fun getItemViewType(i: Int): Int {
        return when (summaries[i]) {
            is NotificationSummary -> NOTIFICATION
            is MediaSummary -> MEDIA
            else -> throw Exception("Wrong summary type")
        }
    }

    var mediaSummaryViewHolder: MediaSummaryViewHolder? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SummaryViewHolder {
        return when (viewType) {
            NOTIFICATION -> NotificationSummaryViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_summary_notification, parent, false))
            MEDIA -> MediaSummaryViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_summary_media, parent, false)).also { mediaSummaryViewHolder = it }
            else -> throw Exception("Wrong view type")
        }
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, i: Int) {
        val summary = summaries[i]
        holder.onBind(summary)
    }

    override fun getItemCount() = summaries.size

    fun updateSummaries(summaries: List<SummaryItem>) {
        this.summaries = summaries
        notifyDataSetChanged()
    }

    fun onScroll() {
        mediaSummaryViewHolder?.blurBG?.invalidate()
    }

    companion object {
        const val NOTIFICATION = 0
        const val MEDIA = 1
    }
}