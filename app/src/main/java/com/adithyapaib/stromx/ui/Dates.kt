package com.adithyapaib.stromx.ui

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val zone: ZoneId get() = ZoneId.systemDefault()
private val time = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
private val dayMonth = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
private val weekdayDayMonth = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())
private val weekdayDayMonthLong = DateTimeFormatter.ofPattern("EEE d MMMM", Locale.getDefault())
private val monthName = DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())

private fun localDate(ms: Long): LocalDate = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()

/** "Today, 7:42 AM", "Yesterday, 6:10 PM", "Mon 24 Nov, 8:05 AM". */
fun formatWhen(ms: Long, nowMs: Long = System.currentTimeMillis()): String {
    val day = localDate(ms)
    val today = localDate(nowMs)
    val clock = Instant.ofEpochMilli(ms).atZone(zone).format(time)
    return when (day) {
        today -> "Today, $clock"
        today.minusDays(1) -> "Yesterday, $clock"
        else -> "${day.format(weekdayDayMonth)}, $clock"
    }
}

/** "Sat 22 Nov". */
fun formatDay(ms: Long): String = localDate(ms).format(weekdayDayMonth)

/** "23 Nov". */
fun formatShortDate(ms: Long): String = localDate(ms).format(dayMonth)

/** "Wed 26 November", for the always-on display. */
fun formatLongDay(ms: Long): String = localDate(ms).format(weekdayDayMonthLong)

/** "2:41", without AM/PM, for the always-on clock. */
fun formatClock(ms: Long): String =
    Instant.ofEpochMilli(ms).atZone(zone).format(DateTimeFormatter.ofPattern("h:mm", Locale.getDefault()))

fun monthLabel(ms: Long): String = YearMonth.from(Instant.ofEpochMilli(ms).atZone(zone)).format(monthName)

fun isSameMonth(a: Long, b: Long): Boolean =
    YearMonth.from(Instant.ofEpochMilli(a).atZone(zone)) == YearMonth.from(Instant.ofEpochMilli(b).atZone(zone))

/** Within the last 7 days, counting today. */
fun isThisWeek(ms: Long, nowMs: Long = System.currentTimeMillis()): Boolean =
    !localDate(ms).isBefore(localDate(nowMs).minusDays(6))
