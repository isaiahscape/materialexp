package com.example.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object RelativeTimeUtils {
    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} minutes ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} hours ago"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
            else -> {
                val sdf = SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    fun formatFullDate(timestamp: Long): String {
        // MiXplorer style: 2026. 7. 29. 오후 4:27
        val sdf = SimpleDateFormat("yyyy. M. d. a h:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
