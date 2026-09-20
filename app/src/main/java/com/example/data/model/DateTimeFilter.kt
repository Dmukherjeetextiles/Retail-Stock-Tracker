package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilterPreset(val label: String) {
  TODAY("Today"),
  YESTERDAY("Yesterday"),
  LAST_7_DAYS("7 Days"),
  THIS_MONTH("This Month"),
  ALL_TIME("All Time"),
  CUSTOM("Custom")
}

data class DateTimeFilter(
  val preset: DateFilterPreset = DateFilterPreset.TODAY,
  val startTimestamp: Long = 0L,
  val endTimestamp: Long = Long.MAX_VALUE,
  val displayLabel: String = "Today",
  val startFormatted: String = "",
  val endFormatted: String = "",
  val rangeSummary: String = "Today (Full Day)"
) {
  val isCustom: Boolean
    get() = preset == DateFilterPreset.CUSTOM

  fun matches(timestamp: Long): Boolean {
    return timestamp in startTimestamp..endTimestamp
  }
}

object DateTimeFilterUtils {
  private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
  private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

  fun createPresetFilter(preset: DateFilterPreset): DateTimeFilter {
    val now = Calendar.getInstance()

    return when (preset) {
      DateFilterPreset.TODAY -> {
        val startCal = Calendar.getInstance().apply {
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
          set(Calendar.HOUR_OF_DAY, 23)
          set(Calendar.MINUTE, 59)
          set(Calendar.SECOND, 59)
          set(Calendar.MILLISECOND, 999)
        }
        val start = startCal.timeInMillis
        val end = endCal.timeInMillis
        DateTimeFilter(
          preset = preset,
          startTimestamp = start,
          endTimestamp = end,
          displayLabel = "Today",
          startFormatted = dateTimeFormat.format(Date(start)),
          endFormatted = dateTimeFormat.format(Date(end)),
          rangeSummary = "Today (${dateFormat.format(Date(start))})"
        )
      }

      DateFilterPreset.YESTERDAY -> {
        val startCal = Calendar.getInstance().apply {
          add(Calendar.DAY_OF_YEAR, -1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
          add(Calendar.DAY_OF_YEAR, -1)
          set(Calendar.HOUR_OF_DAY, 23)
          set(Calendar.MINUTE, 59)
          set(Calendar.SECOND, 59)
          set(Calendar.MILLISECOND, 999)
        }
        val start = startCal.timeInMillis
        val end = endCal.timeInMillis
        DateTimeFilter(
          preset = preset,
          startTimestamp = start,
          endTimestamp = end,
          displayLabel = "Yesterday",
          startFormatted = dateTimeFormat.format(Date(start)),
          endFormatted = dateTimeFormat.format(Date(end)),
          rangeSummary = "Yesterday (${dateFormat.format(Date(start))})"
        )
      }

      DateFilterPreset.LAST_7_DAYS -> {
        val startCal = Calendar.getInstance().apply {
          add(Calendar.DAY_OF_YEAR, -6)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
          set(Calendar.HOUR_OF_DAY, 23)
          set(Calendar.MINUTE, 59)
          set(Calendar.SECOND, 59)
          set(Calendar.MILLISECOND, 999)
        }
        val start = startCal.timeInMillis
        val end = endCal.timeInMillis
        DateTimeFilter(
          preset = preset,
          startTimestamp = start,
          endTimestamp = end,
          displayLabel = "Last 7 Days",
          startFormatted = dateTimeFormat.format(Date(start)),
          endFormatted = dateTimeFormat.format(Date(end)),
          rangeSummary = "${dateFormat.format(Date(start))} – ${dateFormat.format(Date(end))}"
        )
      }

      DateFilterPreset.THIS_MONTH -> {
        val startCal = Calendar.getInstance().apply {
          set(Calendar.DAY_OF_MONTH, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
          set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
          set(Calendar.HOUR_OF_DAY, 23)
          set(Calendar.MINUTE, 59)
          set(Calendar.SECOND, 59)
          set(Calendar.MILLISECOND, 999)
        }
        val start = startCal.timeInMillis
        val end = endCal.timeInMillis
        DateTimeFilter(
          preset = preset,
          startTimestamp = start,
          endTimestamp = end,
          displayLabel = "This Month",
          startFormatted = dateTimeFormat.format(Date(start)),
          endFormatted = dateTimeFormat.format(Date(end)),
          rangeSummary = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(start))
        )
      }

      DateFilterPreset.ALL_TIME -> {
        DateTimeFilter(
          preset = preset,
          startTimestamp = 0L,
          endTimestamp = Long.MAX_VALUE,
          displayLabel = "All Time",
          startFormatted = "Inception",
          endFormatted = "Present",
          rangeSummary = "All Historical Data"
        )
      }

      DateFilterPreset.CUSTOM -> {
        // Default custom to today full day
        createPresetFilter(DateFilterPreset.TODAY).copy(
          preset = DateFilterPreset.CUSTOM,
          displayLabel = "Custom Range"
        )
      }
    }
  }

  fun createCustomFilter(
    startTimestamp: Long,
    endTimestamp: Long,
    customLabel: String = ""
  ): DateTimeFilter {
    val startStr = dateTimeFormat.format(Date(startTimestamp))
    val endStr = dateTimeFormat.format(Date(endTimestamp))
    val label = if (customLabel.isNotBlank()) customLabel else "$startStr to $endStr"

    return DateTimeFilter(
      preset = DateFilterPreset.CUSTOM,
      startTimestamp = startTimestamp,
      endTimestamp = endTimestamp,
      displayLabel = "Custom",
      startFormatted = startStr,
      endFormatted = endStr,
      rangeSummary = label
    )
  }
}
