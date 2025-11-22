package com.example.testeva2.menus
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.componentestest.Componentes.Firebase.SensorData
import com.example.testeva2.currentGasLevel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database


@Composable
fun EjemploLectura(){
    var sensor by remember  { mutableStateOf<SensorData?>(null) }

    LaunchedEffect(Unit) {
        val database = Firebase.database
        val sensorRef = database.getReference("sensor_data")

        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val value = snapshot.getValue(SensorData::class.java)
                    sensor = value

                    currentGasLevel = value?.raw_value ?: 0

                } catch (e: Exception) {
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    Text(
        text = "Sensor: ${sensor?.raw_value ?: 0} ppm",
        modifier = Modifier.padding(16.dp)
    )
}