package com.flores.educore

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.postgrest.postgrest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaEstudianteScreen(
    alumnoId: Int, // <-- AHORA RECIBE EL ID DE QUIEN INICIÓ SESIÓN
    onBackClick: () -> Unit
) {
    var presentes by remember { mutableFloatStateOf(0f) }
    var faltas by remember { mutableFloatStateOf(0f) }
    var tardanzas by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(alumnoId) {
        try {
            // Buscamos solo el historial de ESTE alumno específico
            val asistenciasNube = supabase.postgrest["asistencias"]
                .select {
                    filter { eq("alumno_id", alumnoId) } // <-- ¡LA MAGIA! Filtro dinámico
                }
                .decodeList<AsistenciaRegistro>()

            presentes = asistenciasNube.count { it.estado == "Presente" }.toFloat()
            faltas = asistenciasNube.count { it.estado == "Falta" }.toFloat()
            tardanzas = asistenciasNube.count { it.estado == "Tardanza" }.toFloat()

        } catch (e: Throwable) {
            println("Error al cargar asistencias: ${e.message}")
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    val total = presentes + faltas + tardanzas

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Asistencia", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Diseño y Programación Web", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(40.dp))
            } else if (total == 0f) {
                Text("Aún no tienes asistencias registradas.", color = Color.Gray)
            } else {
                PieChart(presentes = presentes, faltas = faltas, tardanzas = tardanzas, modifier = Modifier.size(220.dp).padding(16.dp))
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Resumen del Semestre:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                        LeyendaItem(color = Color(0xFF4CAF50), texto = "Presente", cantidad = presentes.toInt(), total = total.toInt())
                        LeyendaItem(color = Color(0xFFF44336), texto = "Falta", cantidad = faltas.toInt(), total = total.toInt())
                        LeyendaItem(color = Color(0xFFFF9800), texto = "Tardanza", cantidad = tardanzas.toInt(), total = total.toInt())
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(presentes: Float, faltas: Float, tardanzas: Float, modifier: Modifier = Modifier) {
    val total = presentes + faltas + tardanzas
    if (total == 0f) return
    val gradosPresentes = (presentes / total) * 360f
    val gradosFaltas = (faltas / total) * 360f
    val gradosTardanzas = (tardanzas / total) * 360f

    Canvas(modifier = modifier) {
        var startAngle = -90f
        if (presentes > 0) { drawArc(color = Color(0xFF4CAF50), startAngle = startAngle, sweepAngle = gradosPresentes, useCenter = true); startAngle += gradosPresentes }
        if (faltas > 0) { drawArc(color = Color(0xFFF44336), startAngle = startAngle, sweepAngle = gradosFaltas, useCenter = true); startAngle += gradosFaltas }
        if (tardanzas > 0) { drawArc(color = Color(0xFFFF9800), startAngle = startAngle, sweepAngle = gradosTardanzas, useCenter = true) }
    }
}

@Composable
fun LeyendaItem(color: Color, texto: String, cantidad: Int, total: Int) {
    val porcentaje = if (total > 0) (cantidad * 100) / total else 0
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = texto, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(text = "$cantidad ($porcentaje%)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}