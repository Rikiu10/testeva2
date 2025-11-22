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
import com.example.testeva2.LedConfig
import com.example.testeva2.ui.theme.Testeva2Theme

class ActivityLedRange : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Testeva2Theme {
                LedRangeScreen()
            }
        }
    }
}

@Composable
fun LedRangeScreen() {
    var warningLevelInput by rememberSaveable { mutableStateOf("50") }
    var criticalLevelInput by rememberSaveable { mutableStateOf("100") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "1. Rangos de Activación de Alerta",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = warningLevelInput,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                    warningLevelInput = newValue
                }
            },
            label = { Text("Nivel de Advertencia (ppm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        OutlinedTextField(
            value = criticalLevelInput,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                    criticalLevelInput = newValue
                }
            },
            label = { Text("Nivel Crítico (ppm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Button(
            onClick = {
                val warning = warningLevelInput.toIntOrNull() ?: 0
                val critical = criticalLevelInput.toIntOrNull() ?: 0

                if (critical <= warning) {
                    Toast.makeText(context, "El Nivel Crítico debe ser mayor que el de Advertencia.", Toast.LENGTH_LONG).show()
                    return@Button
                }

                val config = LedConfig(warningLevel = warning, criticalLevel = critical)

                escribirFirebaseGenerico(
                    field = "app_config/led_ranges",
                    value = config,
                    onSuccess = {
                        Toast.makeText(context, "✅ Rangos guardados con éxito.", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(context, "❌ Error al guardar: $it", Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Configuración")
        }
    }
}