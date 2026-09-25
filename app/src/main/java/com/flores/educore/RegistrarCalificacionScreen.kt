package com.flores.educore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// Molde para enviar la nueva nota a Supabase
@Serializable
data class CalificacionInsert(
    val alumno_id: Long,
    val curso: String,
    val nota: String,
    val periodo: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarCalificacionScreen(onBackClick: () -> Unit) {
    var alumnoIdText by remember { mutableStateOf("") }
    var curso by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var periodo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var mensajeEstado by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Calificación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ingrese los datos de la nota para el alumno",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = alumnoIdText,
                onValueChange = { alumnoIdText = it },
                label = { Text("ID del Alumno (Ej: 2 para Benji)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = curso,
                onValueChange = { curso = it },
                label = { Text("Curso (Ej: Diseño Web)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nota,
                onValueChange = { nota = it },
                label = { Text("Nota (Ej: 18)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = periodo,
                onValueChange = { periodo = it },
                label = { Text("Periodo / Unidad (Ej: Unidad I)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (mensajeEstado.isNotEmpty()) {
                Text(
                    text = mensajeEstado,
                    color = if (mensajeEstado.contains("éxito")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    val idNum = alumnoIdText.toLongOrNull()
                    if (idNum == null || curso.isEmpty() || nota.isEmpty() || periodo.isEmpty()) {
                        mensajeEstado = "Por favor completa todos los campos correctamente"
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        try {
                            mensajeEstado = ""
                            val nuevaCalificacion = CalificacionInsert(
                                alumno_id = idNum,
                                curso = curso,
                                nota = nota,
                                periodo = periodo
                            )

                            // Insertamos directamente en la tabla calificaciones de Supabase
                            supabase.postgrest["calificaciones"].insert(nuevaCalificacion)

                            mensajeEstado = "¡Calificación registrada con éxito!"
                            alumnoIdText = ""
                            curso = ""
                            nota = ""
                            periodo = ""
                        } catch (e: Throwable) {
                            e.printStackTrace()
                            mensajeEstado = "Error al registrar: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Guardando..." else "Guardar Calificación", fontSize = 16.sp)
            }
        }
    }
}