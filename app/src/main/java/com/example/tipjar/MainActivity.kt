package com.example.tipjar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// The tip math, kept apart from the screen so it can be unit tested.
object Tip {
    fun perPersonCents(billCents: Int, tipPercent: Int, people: Int, roundUp: Boolean): Int {
        val total = billCents + billCents * tipPercent / 100
        val share = (total + people - 1) / people
        return if (roundUp) ((share + 99) / 100) * 100 else share
    }

    fun money(cents: Int): String = "$" + (cents / 100) + "." + (cents % 100).toString().padStart(2, '0')
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) { TipScreen() }
            }
        }
    }
}

// Every button on this screen changes what is shown.
@Composable
fun TipScreen() {
    var billCents by rememberSaveable { mutableIntStateOf(5000) }
    var tipPercent by rememberSaveable { mutableIntStateOf(15) }
    var people by rememberSaveable { mutableIntStateOf(2) }
    var roundUp by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Tip Jar", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Bill " + Tip.money(billCents), fontSize = 22.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { billCents = maxOf(0, billCents - 500) }) { Text("- $5") }
            OutlinedButton(onClick = { billCents += 500 }) { Text("+ $5") }
        }
        Text("Tip $tipPercent%")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (p in listOf(10, 18, 20, 25)) {
                Button(onClick = { tipPercent = p }) { Text("$p%") }
            }
        }
        Text("$people people")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { people = maxOf(1, people - 1) }) { Text("Fewer") }
            OutlinedButton(onClick = { people += 1 }) { Text("More") }
        }
        OutlinedButton(onClick = { roundUp = !roundUp }) { Text(if (roundUp) "Exact amount" else "Round up") }
        Text("Each pays " + Tip.money(Tip.perPersonCents(billCents, tipPercent, people, roundUp)), fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}
