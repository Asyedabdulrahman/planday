package com.planday.app.domain.parser

import com.planday.app.data.local.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class QuickAddParserTest {

    private val baseDate = LocalDate.of(2026, 10, 17) // Saturday

    @Test
    fun parse_fullString_extractsAllFieldsCorrectly() {
        val input = "Sliding window 2 problems tomorrow 9pm 60m"
        val result = QuickAddParser.parse(input, baseDate = baseDate)

        assertEquals("Sliding window 2 problems", result.title)
        assertEquals(baseDate.plusDays(1), result.date)
        assertEquals(LocalTime.of(21, 0), result.time)
        assertEquals(60, result.estimateMin)
        assertEquals(TaskPriority.P2, result.priority)
    }

    @Test
    fun parse_withProjectAndPriority_extractsSuccessfully() {
        val input = "Complete WAL component P1 #KV-Project 2h today"
        val result = QuickAddParser.parse(input, baseDate = baseDate)

        assertEquals("Complete WAL component", result.title)
        assertEquals(baseDate, result.date)
        assertEquals(120, result.estimateMin)
        assertEquals(TaskPriority.P1, result.priority)
        assertEquals("KV-Project", result.project)
        assertNull(result.time)
    }

    @Test
    fun parse_withNamedWeekday_resolvesCorrectDate() {
        val input = "Mock interview on monday 3pm 45m"
        val result = QuickAddParser.parse(input, baseDate = baseDate) // Saturday 2026-10-17

        assertEquals("Mock interview", result.title)
        assertEquals(LocalDate.of(2026, 10, 19), result.date) // Monday
        assertEquals(LocalTime.of(15, 0), result.time)
        assertEquals(45, result.estimateMin)
    }

    @Test
    fun parse_plainTextOnly_preservesTitleAndDefaults() {
        val input = "Read chapter 4 of Designing Data-Intensive Applications"
        val result = QuickAddParser.parse(input, baseDate = baseDate)

        assertEquals("Read chapter 4 of Designing Data-Intensive Applications", result.title)
        assertNull(result.date)
        assertNull(result.time)
        assertNull(result.estimateMin)
        assertEquals(TaskPriority.P2, result.priority)
    }
}
