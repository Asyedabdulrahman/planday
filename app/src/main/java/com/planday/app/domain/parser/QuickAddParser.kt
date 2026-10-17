package com.planday.app.domain.parser

import com.planday.app.core.util.DateTimeUtils
import com.planday.app.data.local.model.TaskPriority
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import java.util.regex.Pattern

data class ParsedQuickTask(
    val title: String,
    val date: LocalDate?,
    val time: LocalTime?,
    val estimateMin: Int?,
    val priority: TaskPriority,
    val project: String?
)

object QuickAddParser {

    private val TIME_PATTERN = Pattern.compile("(?i)\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b")
    private val TIME_24H_PATTERN = Pattern.compile("(?i)\\b(\\d{1,2}):(\\d{2})\\b")
    private val ESTIMATE_PATTERN = Pattern.compile("(?i)\\b(\\d+)\\s*(m|min|mins|minutes|h|hr|hrs|hours)\\b")
    private val PRIORITY_PATTERN = Pattern.compile("(?i)\\b(p[1-3])\\b")
    private val PROJECT_PATTERN = Pattern.compile("(?i)#[a-zA-Z0-9_-]+")

    fun parse(rawText: String, baseDate: LocalDate = DateTimeUtils.today()): ParsedQuickTask {
        var remaining = rawText.trim()
        var date: LocalDate? = null
        var time: LocalTime? = null
        var estimateMin: Int? = null
        var priority: TaskPriority = TaskPriority.P2
        var project: String? = null

        // 1. Project extraction (e.g., #DSA, #KV-Project)
        val projectMatcher = PROJECT_PATTERN.matcher(remaining)
        if (projectMatcher.find()) {
            project = projectMatcher.group().removePrefix("#")
            remaining = projectMatcher.replaceFirst("").trim()
        }

        // 2. Priority extraction (e.g., P1, P2, P3)
        val priorityMatcher = PRIORITY_PATTERN.matcher(remaining)
        if (priorityMatcher.find()) {
            val pStr = priorityMatcher.group().uppercase(Locale.ENGLISH)
            priority = when (pStr) {
                "P1" -> TaskPriority.P1
                "P3" -> TaskPriority.P3
                else -> TaskPriority.P2
            }
            remaining = priorityMatcher.replaceFirst("").trim()
        }

        // 3. Estimate extraction (e.g., 60m, 2h, 30mins)
        val estimateMatcher = ESTIMATE_PATTERN.matcher(remaining)
        if (estimateMatcher.find()) {
            val num = estimateMatcher.group(1)?.toIntOrNull() ?: 0
            val unit = estimateMatcher.group(2)?.lowercase(Locale.ENGLISH) ?: "m"
            estimateMin = if (unit.startsWith("h")) num * 60 else num
            remaining = estimateMatcher.replaceFirst("").trim()
        }

        // 4. Date extraction: today, tomorrow, day of week
        val lowerRemaining = remaining.lowercase(Locale.ENGLISH)
        if (lowerRemaining.contains("tomorrow")) {
            date = baseDate.plusDays(1)
            remaining = remaining.replace("(?i)\\btomorrow\\b".toRegex(), "").trim()
        } else if (lowerRemaining.contains("today")) {
            date = baseDate
            remaining = remaining.replace("(?i)\\btoday\\b".toRegex(), "").trim()
        } else {
            // Check day names (e.g. "on monday", "saturday")
            val daysOfWeek = java.time.DayOfWeek.values()
            for (dow in daysOfWeek) {
                val dowName = dow.name.lowercase(Locale.ENGLISH)
                val dowRegex = "(?i)\\b(on\\s+)?$dowName\\b".toRegex()
                if (remaining.contains(dowRegex)) {
                    var targetDate = baseDate.plusDays(1)
                    while (targetDate.dayOfWeek != dow) {
                        targetDate = targetDate.plusDays(1)
                    }
                    date = targetDate
                    remaining = remaining.replace(dowRegex, "").trim()
                    break
                }
            }
        }

        // 5. Time extraction (e.g., 9pm, 9:30am, 21:00)
        val timeMatcher = TIME_PATTERN.matcher(remaining)
        if (timeMatcher.find()) {
            var hour = timeMatcher.group(1)?.toIntOrNull() ?: 0
            val minute = timeMatcher.group(2)?.toIntOrNull() ?: 0
            val ampm = timeMatcher.group(3)?.lowercase(Locale.ENGLISH)
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0
            time = LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            remaining = timeMatcher.replaceFirst("").trim()
        } else {
            val time24Matcher = TIME_24H_PATTERN.matcher(remaining)
            if (time24Matcher.find()) {
                val hour = time24Matcher.group(1)?.toIntOrNull() ?: 0
                val minute = time24Matcher.group(2)?.toIntOrNull() ?: 0
                if (hour in 0..23 && minute in 0..59) {
                    time = LocalTime.of(hour, minute)
                    remaining = time24Matcher.replaceFirst("").trim()
                }
            }
        }

        // Clean up remaining whitespace and extra punctuation for the clean title
        val cleanedTitle = remaining.replace("\\s+".toRegex(), " ").trim()
        val finalTitle = if (cleanedTitle.isNotBlank()) cleanedTitle else rawText.trim()

        return ParsedQuickTask(
            title = finalTitle,
            date = date,
            time = time,
            estimateMin = estimateMin,
            priority = priority,
            project = project
        )
    }
}
