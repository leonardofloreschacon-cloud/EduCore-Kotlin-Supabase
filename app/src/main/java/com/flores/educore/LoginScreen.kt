package com.flores.educore

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

// Molde para extraer datos de los profesores
@Serializable
data class UsuarioRol(
    val id: Int,
    val rol: String
)

// NUEVO: Molde para extraer solo el ID real de los estudiantes
@Serializable
data class EstudianteLogin(
    val id: Int
)

@Composable
fun LoginScreen(onLoginSuccess: (String, Int) -> Unit, onNavigateToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("EduCore", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Acceso para Móviles", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if(email.isEmpty() || password.isEmpty()) { errorMessage = "Llena todos los campos"; return@Button }
                scope.launch {
                    isLoading = true
                    try {
                        errorMessage = ""
                        supabase.auth.signInWith(Email) {
                            this.email = email
                            this.password = password
                        }

                        // 1. Buscamos primero si el correo pertenece a un Profesor/Admin en la tabla "usuarios"
                        val usuario = supabase.postgrest["usuarios"]
                            .select { filter { eq("correo", email) } }
                            .decodeSingleOrNull<UsuarioRol>()

                        var rolFinal = "Estudiante"
                        var idFinal = 0

                        if (usuario != null) {
                            // Si lo encuentra, es un Profesor
                            rolFinal = usuario.rol
                            idFinal = usuario.id
                        } else {
                            // 2. Si no es profesor, buscamos su ID real en la tabla "estudiantes"
                            val estudiante = supabase.postgrest["estudiantes"]
                                .select { filter { eq("correo", email) } }
                                .decodeSingleOrNull<EstudianteLogin>()

                            if (estudiante != null) {
                                // ¡Encontramos al estudiante! Le asignamos su ID real (Ej: el ID 6 de Maria)
                                rolFinal = "Estudiante"
                                idFinal = estudiante.id
                            } else {
                                // Si se registró en la app pero tú aún no lo agregas en el Panel Web:
                                errorMessage = "Tu cuenta existe, pero el instituto aún no ha validado tu matrícula."
                                isLoading = false
                                return@launch
                            }
                        }

                        // Enviamos a la persona a la pantalla correcta con su ID exacto
                        onLoginSuccess(rolFinal, idFinal)

                    } catch (e: Throwable) {
                        e.printStackTrace()
                        errorMessage = "Credenciales incorrectas"
                    } finally { isLoading = false }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Ingresando..." else "Iniciar Sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToRegister) { Text("¿No tienes cuenta? Regístrate aquí") }
    }
}