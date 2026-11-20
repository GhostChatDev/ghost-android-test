package com.example.water

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class WaterLogicTest {
    private val zone = ZoneId.of("America/Toronto")
    private val today = LocalDate.of(2025, 3, 10)
    private fun drink(id: String, amount: Int, time: String) = Drink(id, amount, Instant.parse(time).toEpochMilli())
    private val drinks = listOf(
        drink("a", 250, "2025-03-10T12:00:00Z"),
        drink("b", 500, "2025-03-10T13:00:00Z"),
        drink("c", 250, "2025-03-10T02:00:00Z"),
    )

    @Test fun todayUsesLocalDateAndNewestFirst() {
        val result = WaterLogic.drinksOn(drinks, today, zone)
        assertEquals(listOf("b", "a"), result.map { it.id })
        assertEquals(750, WaterLogic.total(result))
    }

    @Test fun weekIncludesSevenDaysInOrderIncludingEmptyDays() {
        val week = WaterLogic.week(drinks, today, zone)
        assertEquals(7, week.size)
        assertEquals(today.minusDays(6), week.first().date)
        assertEquals(250, week[5].amountMl)
        assertEquals(750, week[6].amountMl)
        assertEquals(0, week[0].amountMl)
    }

    @Test fun progressIsClamped() {
        assertEquals(0.375f, WaterLogic.progress(750, 2000), 0.001f)
        assertEquals(1f, WaterLogic.progress(3000, 2000), 0.001f)
        assertEquals(0f, WaterLogic.progress(100, 0), 0.001f)
    }

    @Test fun goalMustBeWithinBounds() {
        assertEquals(2000, WaterLogic.validGoal("2000"))
        assertNull(WaterLogic.validGoal("0"))
        assertNull(WaterLogic.validGoal("100001"))
        assertNull(WaterLogic.validGoal("oops"))
    }
}
