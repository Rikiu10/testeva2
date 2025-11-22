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
// 🛠️ Importaciones clave del paquete raíz:
import com.example.testeva2.menus.ContactConfig
import com.example.testeva2.menus.escribirFirebaseGenerico
import com.example.testeva2.ui.theme.Testeva2Theme

class ActivityCriticalContacts : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Testeva2Theme {
                CriticalContactsScreen()
            }
        }
    }
}

@Composable
fun CriticalContactsScreen() {
    var name1 by rememberSaveable { mutableStateOf("") }
    var number1 by rememberSaveable { mutableStateOf("") }
    var name2 by rememberSaveable { mutableStateOf("") }
    var number2 by rememberSaveable { mutableStateOf("") }
    var callEnabled by rememberSaveable { mutableStateOf(true) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "4. Configurar Contactos Críticos",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Switch para habilitar/deshabilitar la función de llamada
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Habilitar Llamada de Emergencia")
            Switch(checked = callEnabled, onCheckedChange = { callEnabled = it })
        }

        Spacer(Modifier.height(8.dp))
        Divider()
        Text("Contacto Principal", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))

        // --- Contacto 1 ---
        ContactInput(
            label = "Nombre 1",
            value = name1,
            onValueChange = { name1 = it }
        )
        ContactInput(
            label = "Teléfono 1",
            value = number1,
            onValueChange = { number1 = it },
            keyboardType = KeyboardType.Phone
        )

        Spacer(Modifier.height(24.dp))
        Text("Contacto Secundario", style = MaterialTheme.typography.titleMedium)

        // --- Contacto 2 ---
        ContactInput(
            label = "Nombre 2",
            value = name2,
            onValueChange = { name2 = it }
        )
        ContactInput(
            label = "Teléfono 2",
            value = number2,
            onValueChange = { number2 = it },
            keyboardType = KeyboardType.Phone
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (name1.isEmpty() || number1.isEmpty()) {
                    Toast.makeText(context, "El Contacto Principal es obligatorio.", Toast.LENGTH_LONG).show()
                    return@Button
                }

                val config = ContactConfig(
                    contact_name_1 = name1,
                    contact_number_1 = number1,
                    contact_name_2 = name2,
                    contact_number_2 = number2,
                    call_enabled = callEnabled
                )

                escribirFirebaseGenerico(
                    field = "app_config/emergency_contacts",
                    value = config,
                    onSuccess = {
                        Toast.makeText(context, "✅ Contactos guardados con éxito.", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(context, "❌ Error al guardar: $it", Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Contactos")
        }
    }
}

@Composable
fun ContactInput(label: String, value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray
        )
    )
}