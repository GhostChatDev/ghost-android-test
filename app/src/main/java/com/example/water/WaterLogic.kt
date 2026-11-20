package com.example.water

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DayTotal(val date: LocalDate, val amountMl: Int)

object WaterLogic {
    fun drinksOn(drinks: List<Drink>, date: LocalDate, zone: ZoneId): List<Drink> =
        drinks.filter { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() == date }
            .sortedByDescending { it.timestamp }

    fun total(drinks: List<Drink>): Int = drinks.sumOf { it.amountMl }

    fun week(drinks: List<Drink>, today: LocalDate, zone: ZoneId): List<DayTotal> =
        (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            DayTotal(date, total(drinksOn(drinks, date, zone)))
        }

    fun progress(totalMl: Int, goalMl: Int): Float =
        if (goalMl <= 0) 0f else (totalMl.toFloat() / goalMl).coerceIn(0f, 1f)

    fun validGoal(input: String): Int? = input.toIntOrNull()?.takeIf { it in 1..100_000 }
}
