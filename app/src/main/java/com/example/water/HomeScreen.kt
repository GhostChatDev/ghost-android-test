package com.example.water

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HomeScreen(
    snapshot: WaterSnapshot,
    error: String?,
    onAdd: (Int) -> Unit,
    onUndo: () -> Unit,
    onSettings: () -> Unit,
) {
    var today by remember { mutableStateOf(LocalDate.now()) }
    var zone by remember { mutableStateOf(ZoneId.systemDefault()) }
    LaunchedEffect(Unit) {
        while (true) {
            today = LocalDate.now()
            zone = ZoneId.systemDefault()
            delay(30_000)
        }
    }
    val drinks = remember(snapshot.drinks, today, zone) { WaterLogic.drinksOn(snapshot.drinks, today, zone) }
    val total = WaterLogic.total(drinks)
    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("WATER", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("Today", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = onSettings) { Text("Settings") }
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(190.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { WaterLogic.progress(total, snapshot.goalMl) },
                    modifier = Modifier.size(190.dp),
                    strokeWidth = 14.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$total ml", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("of ${snapshot.goalMl} ml", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Text(
            if (total >= snapshot.goalMl) "Daily goal reached!" else "${snapshot.goalMl - total} ml to your daily goal",
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { onAdd(250) }, modifier = Modifier.weight(1f)) { Text("+ 250 ml") }
            Button(onClick = { onAdd(500) }, modifier = Modifier.weight(1f)) { Text("+ 500 ml") }
        }
        OutlinedButton(onClick = onUndo, enabled = drinks.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
            Text("Undo last drink")
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
        WeekChart(WaterLogic.week(snapshot.drinks, today, zone), snapshot.goalMl)
        Text("Today's drinks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        if (drinks.isEmpty()) {
            Text("No drinks yet. Add your first glass above.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                    drinks.forEach { drink ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${drink.amountMl} ml", fontWeight = FontWeight.Medium)
                            Text(Instant.ofEpochMilli(drink.timestamp).atZone(zone).format(formatter))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
