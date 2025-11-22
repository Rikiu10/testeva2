package com.example.testeva2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.testeva2.menus.ActivityCriticalContacts
import com.example.testeva2.menus.ActivityHistory
import com.example.testeva2.menus.EjemploLectura
import com.example.testeva2.menus.ActivityLedRange
import com.example.testeva2.menus.ActivityTimeRange
import com.example.testeva2.ui.theme.Testeva2Theme
import com.google.firebase.database.*
import java.util.Calendar


var currentGasLevel by mutableIntStateOf(0)


data class LedConfig(
    val warningLevel: Int = 100,
    val criticalLevel: Int = 150
) { constructor() : this(100, 150) } // Constructor vacío para Firebase

data class TimeConfig(
    val enabled: Boolean = false,
    val startHour: Int = 0,
    val startMinute: Int = 0,
    val endHour: Int = 23,
    val endMinute: Int = 59
) { constructor() : this(false, 0, 0, 23, 59) }

data class ContactConfig(
    val callEnabled: Boolean = false,
    val contactNumber1: String = ""
) { constructor() : this(false, "") }



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Testeva2Theme {
                MainScreen()
            }
        }
    }
}



@Composable
fun MainScreen() {
    val context = LocalContext.current


    val onNavigate = { destinationClass: Class<*> ->
        context.startActivity(Intent(context, destinationClass))
    }


    var ledConfig by remember { mutableStateOf(LedConfig()) }
    FirebaseObjectListener("app_config/led_ranges", LedConfig::class.java) { config ->
        ledConfig = config
    }

    var timeConfig by remember { mutableStateOf(TimeConfig()) }
    FirebaseObjectListener("app_config/time_ranges", TimeConfig::class.java) { config ->
        timeConfig = config
    }

    var contactConfig by remember { mutableStateOf(ContactConfig()) }
    FirebaseObjectListener("app_config/emergency_contacts", ContactConfig::class.java) { config ->
        contactConfig = config
    }


    val gasLevel = currentGasLevel


    LaunchedEffect(gasLevel, ledConfig, timeConfig, contactConfig) {
        if (ledConfig.criticalLevel > 0 && contactConfig.contactNumber1.isNotEmpty()) {
            checkForCriticalAlert(
                context = context,
                gasLevel = gasLevel,
                ledConfig = ledConfig,
                timeConfig = timeConfig,
                contactConfig = contactConfig
            )
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Detector de Gas IoT",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )


            EjemploLectura()

            Spacer(Modifier.height(16.dp))


            val ledColor = determineLedColor(gasLevel, ledConfig)
            Text(
                text = "Estado del Gas: Nivel $gasLevel ppm",
                color = ledColor,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(Modifier.height(32.dp))


            Text(
                text = "Opciones de Configuración",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            MenuButton(text = "1. Establecer Rangos de Alerta") {
                onNavigate(ActivityLedRange::class.java)
            }

            MenuButton(text = "2. Establecer Rangos Horarios") {
                onNavigate(ActivityTimeRange::class.java)
            }

            MenuButton(text = "3. Historial de Eventos") {
                onNavigate(ActivityHistory::class.java)
            }

            MenuButton(text = "4. Configurar Contactos Críticos") {
                onNavigate(ActivityCriticalContacts::class.java)
            }
        }
    }
}



@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(text)
    }
}


@Composable
fun <T> FirebaseObjectListener(field: String, valueType: Class<T>, onUpdate: (T) -> Unit) {
    LaunchedEffect(field) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(field)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val value = snapshot.getValue(valueType)
                    if (value != null) {
                        onUpdate(value)
                    } else {

                        onUpdate(valueType.getDeclaredConstructor().newInstance())
                    }
                } catch (_: Exception) {

                    onUpdate(valueType.getDeclaredConstructor().newInstance())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Log de error de Firebase
            }
        })
    }
}


fun determineLedColor(gasLevel: Int, ledConfig: LedConfig): Color {
    return when {
        gasLevel >= ledConfig.criticalLevel -> Color.Red
        gasLevel >= ledConfig.warningLevel -> Color.Yellow
        else -> Color.Green
    }
}


fun checkForCriticalAlert(
    context: Context,
    gasLevel: Int,
    ledConfig: LedConfig,
    timeConfig: TimeConfig,
    contactConfig: ContactConfig
) {

    if (gasLevel < ledConfig.criticalLevel) {
        return
    }

    if (!contactConfig.callEnabled || contactConfig.contactNumber1.isEmpty()) {

        return
    }


    var isTimeAllowed = true
    if (timeConfig.enabled) {
        val now = Calendar.getInstance()
        val startTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeConfig.startHour)
            set(Calendar.MINUTE, timeConfig.startMinute)
        }
        val endTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeConfig.endHour)
            set(Calendar.MINUTE, timeConfig.endMinute)
        }

        isTimeAllowed = if (startTime.before(endTime)) {

            !now.before(startTime) && now.before(endTime)
        } else {

            !now.before(startTime) || now.before(endTime)
        }
    }


    if (isTimeAllowed) {

        initiateCall(context, contactConfig.contactNumber1)
    }
}


fun initiateCall(context: Context, phoneNumber: String) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(context, "Permiso CALL_PHONE no concedido. No se puede llamar.", Toast.LENGTH_LONG).show()
        return
    }

    val intent = Intent(Intent.ACTION_CALL)
    intent.data = "tel:$phoneNumber".toUri()
    try {
        context.startActivity(intent)
        Toast.makeText(context, "🚨 ALERTA CRÍTICA. Iniciando llamada a $phoneNumber.", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al intentar llamar: ${e.message}", Toast.LENGTH_LONG).show()
    }
}



@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    Testeva2Theme {
        MainScreen()
    }
}