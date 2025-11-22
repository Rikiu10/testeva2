package com.example.testeva2.menus
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.database


fun escribirFirebaseGenerico(
    field: String,
    value: Any,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val database = Firebase.database
    val myRef = database.getReference(field)

    Log.d("FirebaseWrite", "Iniciando escritura genérica en: $field")


    myRef.setValue(value)
        .addOnSuccessListener {
            Log.d("FirebaseWrite", "✅ Datos escritos exitosamente en $field")
            onSuccess()
        }
        .addOnFailureListener { error ->
            Log.e("FirebaseWrite", "❌ Error escribiendo en $field", error)
            onError("Error: ${error.message}")
        }
}