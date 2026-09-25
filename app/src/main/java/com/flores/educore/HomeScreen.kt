package com.flores.educore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    rolUsuario: String,
    onLogout: () -> Unit,
    onNavigateToAsistencia: () -> Unit,
    onNavigateToAsistenciaEstudiante: () -> Unit,
    onNavigateToCrearComunicado: () -> Unit,
    onNavigateToVerComunicados: () -> Unit,
    onNavigateToCalificacionesEstudiante: () -> Unit,
    onNavigateToRegistrarCalificacion: () -> Unit // <-- NUEVA ACCIÓN
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "¡Bienvenido a Educore!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Modo actual: $rolUsuario",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (rolUsuario == "Docente") {
            Button(
                onClick = onNavigateToAsistencia,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Tomar Asistencia", fontSize = 16.sp)
            }

            Button(
                onClick = onNavigateToCrearComunicado,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
            ) {
                Text("Publicar Comunicado", fontSize = 16.sp)
            }

            // NUEVO BOTÓN PARA EL DOCENTE
            Button(
                onClick = onNavigateToRegistrarCalificacion,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)) // Naranja profesional
            ) {
                Text("Registrar Calificaciones", fontSize = 16.sp)
            }

        } else if (rolUsuario == "Estudiante") {
            Button(
                onClick = onNavigateToAsistenciaEstudiante,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
            ) {
                Text("Ver Mi Asistencia", fontSize = 16.sp)
            }

            Button(
                onClick = onNavigateToCalificacionesEstudiante,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
            ) {
                Text("Ver Mis Calificaciones", fontSize = 16.sp)
            }
        }

        Button(
            onClick = onNavigateToVerComunicados,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
        ) {
            Text("Ver Comunicados", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cerrar Sesión", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}