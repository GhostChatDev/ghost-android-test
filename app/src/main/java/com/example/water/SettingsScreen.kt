package com.example.water

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    snapshot: WaterSnapshot,
    error: String?,
    onBack: () -> Unit,
    onSaveGoal: (Int) -> Unit,
    onTheme: (String) -> Unit,
) {
    var goal by rememberSaveable { mutableStateOf(snapshot.goalMl.toString()) }
    val parsedGoal = WaterLogic.validGoal(goal)
    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedButton(onClick = onBack) { Text("Back") }
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Daily goal", style = MaterialTheme.typography.titleLarge)
        Text("Choose how much water you want to drink each day.")
        OutlinedTextField(
            value = goal,
            onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) goal = it },
            label = { Text("Goal in ml") },
            singleLine = true,
            isError = parsedGoal == null,
            supportingText = { Text("Enter a goal from 1 to 100,000 ml.") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = { parsedGoal?.let(onSaveGoal) }, enabled = parsedGoal != null, modifier = Modifier.fillMaxWidth()) {
            Text("Save goal")
        }
        Text("Appearance", style = MaterialTheme.typography.titleLarge)
        listOf("system" to "Use device setting", "light" to "Light", "dark" to "Dark").forEach { (value, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = snapshot.theme == value, onClick = { onTheme(value) })
                Text(label)
            }
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
    }
}
