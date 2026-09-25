package com.flores.educore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.launch

// NUEVAS IMPORTACIONES PARA LA BASE DE DATOS
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

// 1. EL MOLDE DE SUPABASE: Cómo viene la info desde la nube
@Serializable
data class Estudiante(
    val id: Int,
    val nombres: String = "",
    val apellidos: String = ""
)

// 3. EL MOLDE PARA ENVIAR: Cómo empaquetamos la info hacia la nube
@Serializable
data class AsistenciaRegistro(
    val fecha: String,
    val alumno_id: Int,
    val alumno_nombre: String,
    val estado: String
)

// 2. EL MOLDE VISUAL: Lo que usa la pantalla (incluye el estado de asistencia táctil)
data class AlumnoAsistencia(
    val id: Int,
    val nombreCompleto: String,
    var estado: String = "" // "P", "F", "T"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaDocenteScreen(onBackClick: () -> Unit) {
    val scope = rememberCoroutineScope()

    // 3. Lista vacía que guardará los estudiantes reales
    var listaAlumnos by remember { mutableStateOf<List<AlumnoAsistencia>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 4. Descargamos los datos automáticamente al abrir la pantalla
    LaunchedEffect(Unit) {
        try {
            // Traemos la tabla de estudiantes desde Supabase
            val estudiantesNube = supabase.postgrest["estudiantes"]
                .select()
                .decodeList<Estudiante>()

            // Unimos nombres y apellidos y los metemos en la lista visual
            listaAlumnos = estudiantesNube.map { estudiante ->
                AlumnoAsistencia(
                    id = estudiante.id,
                    nombreCompleto = "${estudiante.nombres} ${estudiante.apellidos}"
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            // Si hay error (por ejemplo, sin internet), mostramos un aviso
            listaAlumnos = listOf(
                AlumnoAsistencia(0, e.toString())
            )
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tomar Asistencia", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            var isSaving by remember { mutableStateOf(false) }
            var isSaved by remember { mutableStateOf(false) }

            FloatingActionButton(
                onClick = {
                    // Lógica más segura: Solo ejecutamos si NO está guardando y NO ha guardado ya
                    if (!isSaving && !isSaved) {
                        scope.launch {
                            isSaving = true
                            try {
                                fun obtenerEstadoCompleto(letra: String): String {
                                    return when(letra) {
                                        "P" -> "Presente"
                                        "F" -> "Falta"
                                        "T" -> "Tardanza"
                                        else -> ""
                                    }
                                }

                                val registros = listaAlumnos
                                    .filter { it.estado.isNotEmpty() }
                                    .map { alumno ->
                                        AsistenciaRegistro(
                                            fecha = "2026-09-24",
                                            alumno_id = alumno.id,
                                            alumno_nombre = alumno.nombreCompleto,
                                            estado = obtenerEstadoCompleto(alumno.estado)
                                        )
                                    }

                                if (registros.isNotEmpty()) {
                                    supabase.postgrest["asistencias"].insert(registros)
                                    isSaved = true
                                }
                            } catch (e: Throwable) {
                                // Si falla, imprimirá el error exacto en la pestaña "Logcat" o "Run"
                                println("ERROR AL GUARDAR EN SUPABASE: ${e.message}")
                                e.printStackTrace()
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                },
                containerColor = if (isSaved) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = when {
                        isSaved -> "¡Guardado!"
                        isSaving -> "Enviando..."
                        else -> "Guardar"
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(text = "Diseño y Programación Web", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = "Fecha: 24 de Septiembre, 2026", color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))

            if (isLoading) {
                // Muestra un texto de carga mientras viaja la información desde internet
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listaAlumnos) { alumno ->
                        AlumnoAsistenciaCard(
                            alumno = alumno,
                            onEstadoChanged = { nuevoEstado ->
                                listaAlumnos = listaAlumnos.map {
                                    if (it.id == alumno.id) it.copy(estado = nuevoEstado) else it
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlumnoAsistenciaCard(alumno: AlumnoAsistencia, onEstadoChanged: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = alumno.nombreCompleto,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonAsistencia("P", Color(0xFF4CAF50), alumno.estado == "P") { onEstadoChanged("P") }
                BotonAsistencia("F", Color(0xFFF44336), alumno.estado == "F") { onEstadoChanged("F") }
                BotonAsistencia("T", Color(0xFFFF9800), alumno.estado == "T") { onEstadoChanged("T") }
            }
        }
    }
}

@Composable
fun BotonAsistencia(texto: String, colorBase: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) colorBase else colorBase.copy(alpha = 0.1f))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Text(
                text = texto,
                color = if (isSelected) Color.White else colorBase,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
