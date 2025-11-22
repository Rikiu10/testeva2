package com.example.testeva2.menus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.testeva2.ui.theme.Testeva2Theme
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivityHistory : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Testeva2Theme {
                HistoryScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {
    var historyList by remember { mutableStateOf<List<HistoryRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LoadHistoryData(
        onDataLoaded = { list -> historyList = list; isLoading = false },
        onError = { msg -> errorMessage = msg; isLoading = false }
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("3. Historial de Eventos") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text("Error al cargar: $errorMessage", color = Color.Red)
                historyList.isEmpty() -> Text("No hay registros históricos disponibles.")
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(historyList.reversed()) { record -> // Reversed para ver lo más reciente arriba
                            HistoryItem(record = record)
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(record: HistoryRecord) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }
    val dateString = formatter.format(Date(record.timestamp))

    val alertColor = if (record.is_critical) Color.Red else Color.Green
    val alertText = if (record.is_critical) "🚨 ALERTA CRÍTICA" else "Nivel Normal"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .padding(end = 8.dp)
                .align(Alignment.CenterVertically)
                .background(alertColor, shape = MaterialTheme.shapes.extraSmall)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Nivel de Gas: ${record.gas_level} ppm",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Registro: $dateString",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Text(
            text = alertText,
            color = alertColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun LoadHistoryData(onDataLoaded: (List<HistoryRecord>) -> Unit, onError: (String) -> Unit) {


    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val historyRef = database.getReference("sensor_history")

        historyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<HistoryRecord>()
                for (childSnapshot in snapshot.children) {
                    try {
                        val record = childSnapshot.getValue(HistoryRecord::class.java)
                        record?.let {
                            it.raw_data_key = childSnapshot.key ?: ""
                            list.add(it)
                        }
                    } catch (e: Exception) {
                        onError("Error al mapear un registro: ${e.message}")
                    }
                }
                onDataLoaded(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onError("Error de Firebase: ${error.message}")
            }
        })
    }
}