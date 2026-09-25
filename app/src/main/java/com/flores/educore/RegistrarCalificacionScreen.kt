package com.flores.educore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

@Serializable
data class CalificacionRegistro(
    val alumno_id: Int,
    val alumno_nombre: String,
    val curso: String,
    val nota: Int,
    val periodo: String,
    val fecha: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarCalificacionScreen(onBackClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Estados para los datos de Supabase
    var listaEstudiantes by remember { mutableStateOf<List<Estudiante>>(emptyList()) }

    // Estados para las selecciones del formulario
    var alumnoSeleccionado by remember { mutableStateOf<Estudiante?>(null) }
    var expandedAlumno by remember { mutableStateOf(false) }

    val listaCursos = listOf(
        "Administración de un Sitio Web",
        "Desarrollo de Aplicaciones Empresariales",
        "Despliegue de Aplicaciones Móviles",
        "Documentación en Sistemas",
        "Despliegue de Servicios Web"
    )
    var cursoSeleccionado by remember { mutableStateOf("") }
    var expandedCurso by remember { mutableStateOf(false) }

    val listaPeriodos = listOf("Unidad I", "Unidad II", "Unidad III", "Unidad IV")
    var periodoSeleccionado by remember { mutableStateOf("") }
    var expandedPeriodo by remember { mutableStateOf(false) }

    var notaTexto by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var mensajeEstado by remember { mutableStateOf("") }

    // Cargar alumnos reales al iniciar la pantalla
    LaunchedEffect(Unit) {
        try {
            listaEstudiantes = supabase.postgrest["estudiantes"]
                .select()
                .decodeList<Estudiante>()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Seleccione los datos para evaluar al estudiante",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            // 1. SELECTOR DE ALUMNO
            ExposedDropdownMenuBox(
                expanded = expandedAlumno,
                onExpandedChange = { expandedAlumno = !expandedAlumno }
            ) {
                OutlinedTextField(
                    value = if (alumnoSeleccionado != null) "${alumnoSeleccionado!!.nombres} ${alumnoSeleccionado!!.apellidos}" else "Seleccione un estudiante...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estudiante") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAlumno) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedAlumno,
                    onDismissRequest = { expandedAlumno = false }
                ) {
                    listaEstudiantes.forEach { alumno ->
                        DropdownMenuItem(
                            text = { Text("${alumno.nombres} ${alumno.apellidos}") },
                            onClick = {
                                alumnoSeleccionado = alumno
                                expandedAlumno = false
                            }
                        )
                    }
                }

                // 2. SELECTOR DE CURSO
                ExposedDropdownMenuBox(
                    expanded = expandedCurso,
                    onExpandedChange = { expandedCurso = !expandedCurso }
                ) {
                    OutlinedTextField(
                        value = cursoSeleccionado.ifEmpty { "Seleccione el curso..." },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Curso (IV Ciclo)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCurso) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCurso,
                        onDismissRequest = { expandedCurso = false }
                    ) {
                        listaCursos.forEach { curso ->
                            DropdownMenuItem(
                                text = { Text(curso) },
                                onClick = {
                                    cursoSeleccionado = curso
                                    expandedCurso = false
                                }
                            )
                        }
                    }
                }

                // 3. CAMPO DE NOTA
                OutlinedTextField(
                    value = notaTexto,
                    onValueChange = {
                        if (it.length <= 2) notaTexto = it.filter { char -> char.isDigit() }
                    },
                    label = { Text("Nota (0 - 20)") },
                    placeholder = { Text("Ej: 18") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 4. SELECTOR DE PERIODO
                ExposedDropdownMenuBox(
                    expanded = expandedPeriodo,
                    onExpandedChange = { expandedPeriodo = !expandedPeriodo }
                ) {
                    OutlinedTextField(
                        value = periodoSeleccionado.ifEmpty { "Seleccione el periodo..." },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Periodo Académico") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeriodo) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPeriodo,
                        onDismissRequest = { expandedPeriodo = false }
                    ) {
                        listaPeriodos.forEach { periodo ->
                            DropdownMenuItem(
                                text = { Text(periodo) },
                                onClick = {
                                    periodoSeleccionado = periodo
                                    expandedPeriodo = false
                                }
                            )
                        }
                    }
                }

                if (mensajeEstado.isNotEmpty()) {
                    Text(
                        text = mensajeEstado,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }

                // BOTÓN GUARDAR
                Button(
                    onClick = {
                        if (alumnoSeleccionado == null || cursoSeleccionado.isEmpty() || notaTexto.isEmpty() || periodoSeleccionado.isEmpty()) {
                            mensajeEstado = "Por favor complete todos los campos."
                            return@Button
                        }

                        scope.launch {
                            isSaving = true
                            try {
                                val fechaActual = java.text.SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    java.util.Locale.getDefault()
                                ).format(java.util.Date())

                                val nuevaCalificacion = CalificacionRegistro(
                                    alumno_id = alumnoSeleccionado!!.id,
                                    alumno_nombre = "${alumnoSeleccionado!!.nombres} ${alumnoSeleccionado!!.apellidos}",
                                    curso = cursoSeleccionado,
                                    nota = notaTexto.toInt(),
                                    periodo = periodoSeleccionado,
                                    fecha = fechaActual
                                )

                                supabase.postgrest["calificaciones"].insert(nuevaCalificacion)
                                mensajeEstado = "¡Calificación guardada con éxito!"

                                // Limpiar campos
                                alumnoSeleccionado = null
                                cursoSeleccionado = ""
                                notaTexto = ""
                                periodoSeleccionado = ""
                            } catch (e: Exception) {
                                mensajeEstado = "Error al guardar: ${e.message}"
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isSaving
                ) {
                    Text(
                        text = if (isSaving) "Guardando..." else "Guardar Calificación",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}