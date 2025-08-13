package com.kurt.mynoteapp.util

import java.util.Locale

object DateFormatUtils {
    private const val PATTERN = "dd MMM yyyy • HH:mm"

    // Thread-safe cache - her thread için ayrı formatter
    private val formatterCache = ThreadLocal.withInitial {
        java.text.SimpleDateFormat(PATTERN, Locale.getDefault())
    }

    fun format(tsMillis: Long, locale: Locale = Locale.getDefault()): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Modern API: java.time kullanımı
            val formatter = java.time.format.DateTimeFormatter.ofPattern(PATTERN).withLocale(locale)
            java.time.Instant.ofEpochMilli(tsMillis)
                .atZone(java.time.ZoneId.systemDefault())
                .format(formatter)
        } else {
            // Fallback: SimpleDateFormat + ThreadLocal cache
            formatterCache.get()?.format(java.util.Date(tsMillis))
                ?: java.text.SimpleDateFormat(PATTERN, locale).format(java.util.Date(tsMillis))
        }
    }
}