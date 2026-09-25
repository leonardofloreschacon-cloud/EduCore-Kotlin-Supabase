package com.flores.educore

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Molde para enviar los datos a Supabase
@Serializable
data class ComunicadoInsert(
    val titulo: String,
    val fecha: String,
    val contenido: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearComunicadoScreen(onBackClick: () -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var mensajeEstado by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Comunicado", fontWeight = FontWeight.Bold) },
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
                text = "Publicar aviso para los estudiantes",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del comunicado") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = contenido,
                onValueChange = { contenido = it },
                label = { Text("Contenido o mensaje") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp), // Campo de texto más grande tipo caja
                maxLines = 5
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
                    if (titulo.isEmpty() || contenido.isEmpty()) {
                        mensajeEstado = "Por favor completa todos los campos"
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        try {
                            mensajeEstado = ""

                            // Generamos la fecha actual en texto (Ej: "24 de Septiembre, 2026")
                            val fechaActual = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES")).format(Date())

                            val nuevoAviso = ComunicadoInsert(
                                titulo = titulo,
                                fecha = fechaActual,
                                contenido = contenido
                            )

                            // Insertamos en la tabla comunicados de Supabase
                            supabase.postgrest["comunicados"].insert(nuevoAviso)

                            mensajeEstado = "¡Comunicado publicado con éxito!"
                            titulo = ""
                            contenido = ""
                        } catch (e: Throwable) {
                            e.printStackTrace()
                            mensajeEstado = "Error al publicar: ${e.message}"
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
                Text(if (isLoading) "Publicando..." else "Publicar Comunicado", fontSize = 16.sp)
            }
        }
    }
}