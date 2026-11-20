package com.example.water

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

private val Context.waterDataStore by preferencesDataStore(name = "water")

data class Drink(val id: String, val amountMl: Int, val timestamp: Long)
data class WaterSnapshot(
    val goalMl: Int = 2000,
    val theme: String = "system",
    val drinks: List<Drink> = emptyList(),
)

class WaterRepository(context: Context) {
    private val store = context.applicationContext.waterDataStore
    private val goalKey = intPreferencesKey("goal_ml")
    private val themeKey = stringPreferencesKey("theme")
    private val drinksKey = stringPreferencesKey("drinks")

    val data = store.data.map { preferences ->
        WaterSnapshot(
            goalMl = (preferences[goalKey] ?: 2000).coerceAtLeast(1),
            theme = preferences[themeKey] ?: "system",
            drinks = decode(preferences[drinksKey] ?: "[]"),
        )
    }

    suspend fun addDrink(amountMl: Int) {
        require(amountMl > 0)
        store.edit { preferences ->
            val drinks = decode(preferences[drinksKey] ?: "[]")
            val drink = Drink(UUID.randomUUID().toString(), amountMl, System.currentTimeMillis())
            preferences[drinksKey] = encode(drinks + drink)
        }
    }

    suspend fun undoToday(date: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault()) {
        store.edit { preferences ->
            val drinks = decode(preferences[drinksKey] ?: "[]")
            val last = drinks.filter {
                Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() == date
            }.maxByOrNull { it.timestamp }
            if (last != null) preferences[drinksKey] = encode(drinks.filterNot { it.id == last.id })
        }
    }

    suspend fun setGoal(goalMl: Int) {
        require(goalMl in 1..100_000)
        store.edit { it[goalKey] = goalMl }
    }

    suspend fun setTheme(theme: String) {
        require(theme in listOf("system", "light", "dark"))
        store.edit { it[themeKey] = theme }
    }

    private fun decode(value: String): List<Drink> {
        val array = JSONArray(value)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            Drink(item.getString("id"), item.getInt("amount"), item.getLong("time"))
        }
    }

    private fun encode(drinks: List<Drink>): String = JSONArray().apply {
        drinks.forEach { drink ->
            put(JSONObject().put("id", drink.id).put("amount", drink.amountMl).put("time", drink.timestamp))
        }
    }.toString()
}
