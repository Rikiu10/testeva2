package com.example.testeva2.menus

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.testeva2.ui.theme.Testeva2Theme

class ActivityTimeRange : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Testeva2Theme {
                TimeRangeScreen()
            }
        }
    }
}

@Composable
fun TimeRangeScreen() {
    var isEnabled by rememberSaveable { mutableStateOf(true) }
    var startHour by rememberSaveable { mutableStateOf("8") }
    var endHour by rememberSaveable { mutableStateOf("20") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "2. Establecer Rangos Horarios",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Habilitar Alerta por Horario")
            Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimeInput(
                label = "Hora de Inicio (0-23)",
                value = startHour,
                onValueChange = { startHour = it },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                enabled = isEnabled
            )
            TimeInput(
                label = "Hora de Fin (0-23)",
                value = endHour,
                onValueChange = { endHour = it },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                enabled = isEnabled
            )
        }

        Button(
            onClick = {
                val startH = startHour.toIntOrNull() ?: 0
                val endH = endHour.toIntOrNull() ?: 0

                if (startH !in 0..23 || endH !in 0..23) {
                    Toast.makeText(context, "Las horas deben estar entre 0 y 23.", Toast.LENGTH_LONG).show()
                    return@Button
                }

                val config = TimeConfig(
                    enabled = isEnabled,
                    start_hour = startH,
                    end_hour = endH,
                )

                escribirFirebaseGenerico(
                    field = "app_config/time_ranges",
                    value = config,
                    onSuccess = {
                        Toast.makeText(context, "✅ Horarios guardados con éxito.", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(context, "❌ Error al guardar: $it", Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEnabled
        ) {
            Text("Guardar Horarios")
        }
    }
}

@Composable
fun TimeInput(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        singleLine = true,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}