package com.example.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Utils {
    fun formatWeekRange(inceptionTimestamp: Long, weekIndex: Int): String {
        val weekStartMillis = inceptionTimestamp + weekIndex * 7L * 24L * 60L * 60L * 1000L
        val weekEndMillis = weekStartMillis + 6L * 24L * 60L * 60L * 1000L
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
        return "${sdf.format(Date(weekStartMillis))} — ${sdf.format(Date(weekEndMillis))}"
    }

    fun getDayAbsoluteDateString(inceptionTimestamp: Long, weekIndex: Int, dayOfWeek: Int): String {
        val cellTimeInMillis = inceptionTimestamp + (weekIndex * 7L + (dayOfWeek - 1)) * 24L * 60L * 60L * 1000L
        val cal = Calendar.getInstance()
        cal.timeInMillis = cellTimeInMillis
        
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val monthVal = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        val dayAbbr = sdf.format(Date(cellTimeInMillis)).uppercase()
        
        val months = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
        val monthAbbr = months[monthVal]
        
        return "$dayAbbr, $dayOfMonth $monthAbbr $year"
    }

    fun getWeekRangeString(inceptionTimestamp: Long, weekIndex: Int): String {
        val startMillis = inceptionTimestamp + (weekIndex * 7L) * 24L * 60L * 60L * 1000L
        val endMillis = startMillis + 6L * 24L * 60L * 60L * 1000L
        
        val startCal = Calendar.getInstance()
        startCal.timeInMillis = startMillis
        
        val endCal = Calendar.getInstance()
        endCal.timeInMillis = endMillis
        
        val startDay = startCal.get(Calendar.DAY_OF_MONTH)
        val startMonthVal = startCal.get(Calendar.MONTH)
        val startYear = startCal.get(Calendar.YEAR)
        
        val endDay = endCal.get(Calendar.DAY_OF_MONTH)
        val endMonthVal = endCal.get(Calendar.MONTH)
        val endYear = endCal.get(Calendar.YEAR)
        
        val months = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
        val startMonth = months[startMonthVal]
        val endMonth = months[endMonthVal]
        
        return when {
            startYear != endYear -> {
                "$startDay $startMonth $startYear - $endDay $endMonth $endYear"
            }
            startMonthVal != endMonthVal -> {
                "$startDay $startMonth - $endDay $endMonth $startYear"
            }
            else -> {
                "$startDay - $endDay $startMonth $startYear"
            }
        }
    }

    fun isCellToday(inceptionTimestamp: Long, weekIndex: Int, dayOfWeek: Int): Boolean {
        val cellTimeInMillis = inceptionTimestamp + (weekIndex * 7L + (dayOfWeek - 1)) * 24L * 60L * 60L * 1000L
        val cal = Calendar.getInstance()
        cal.timeInMillis = cellTimeInMillis
        
        val today = Calendar.getInstance()
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
               cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    fun getTodayDayIndex(inceptionTimestamp: Long, currentWeekIndex: Int): Int {
        val today = Calendar.getInstance()
        for (d in 1..7) {
            val cellTimeInMillis = inceptionTimestamp + (currentWeekIndex * 7L + (d - 1)) * 24L * 60L * 60L * 1000L
            val cal = Calendar.getInstance()
            cal.timeInMillis = cellTimeInMillis
            if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                return d
            }
        }
        // Fallback based on standard day of week
        val calendarDay = today.get(Calendar.DAY_OF_WEEK)
        return when (calendarDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}
