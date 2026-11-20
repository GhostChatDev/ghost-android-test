package com.example.water

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekChart(days: List<DayTotal>, goal: Int) {
    val maximum = maxOf(goal, days.maxOfOrNull { it.amountMl } ?: 0, 1)
    val dateFormat = DateTimeFormatter.ofPattern("MMM d")
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Last 7 days", style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEach { day ->
                    Column(
                        modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {
                            contentDescription = "${day.date.format(dateFormat)}: ${day.amountMl} milliliters"
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("${day.amountMl}", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        Box(Modifier.height(90.dp).width(20.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)), contentAlignment = Alignment.BottomCenter) {
                            if (day.amountMl > 0) {
                                Box(Modifier.fillMaxWidth().fillMaxHeight(day.amountMl.toFloat() / maximum).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)))
                            }
                        }
                        Text(day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 1)
                        Text(day.date.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Text("Amounts in ml · scale includes your goal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
