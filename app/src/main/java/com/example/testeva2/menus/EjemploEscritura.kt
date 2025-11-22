package com.example.testeva2.menus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType



@Composable
fun EjemploEscritura(){
    val context = LocalContext.current
    var valor by rememberSaveable { mutableStateOf(0) }
    Column{

        OutlinedTextField(
            value = valor.toString(),
            onValueChange = { newValue ->
                val intValue = newValue.toIntOrNull()
                if (intValue != null && intValue in 0..100) {
                    valor = intValue
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(
            onClick = {
                var valorEnviar = ActuatorControl(intensity = valor)
                escribirFirebaseGenerico(field = "actuator_control", value = valorEnviar )
            }
        ) {
            Text("Enviar")
        }
    }

}
data class ActuatorControl(
    var enabled: Boolean = false,
    var intensity: Int = 0,
    var last_update: Int = 0,
    var mode: String = "manual"
) {
    constructor() : this(false, 0, 0, "manual")
}