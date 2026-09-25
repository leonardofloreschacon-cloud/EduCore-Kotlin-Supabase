package com.flores.educore

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoginSuccess = { rolRecibido, idRecibido ->
                    // Metemos el rol y el id numérico en la ruta
                    navController.navigate("home/$rolRecibido/$idRecibido") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        // Ruta 2: Pantalla de Registro (¡NUEVA!)
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    // Si se registra con éxito, lo mandamos al home directamente
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateBack = {
                    // Botón para regresar al Login
                    navController.popBackStack()
                }
            )
        }

        // Le avisamos a Compose que esta ruta va a recibir una variable entre llaves {rolUsuario}
        composable("home/{rolUsuario}/{idUsuario}") { backStackEntry ->
            val rolExtraido = backStackEntry.arguments?.getString("rolUsuario") ?: "Estudiante"
            val idExtraido = backStackEntry.arguments?.getString("idUsuario")?.toIntOrNull() ?: 0

            HomeScreen(
                rolUsuario = rolExtraido,
                onLogout = { navController.navigate("login") { popUpTo(0) } },
                onNavigateToAsistencia = { navController.navigate("asistencia") },
                onNavigateToAsistenciaEstudiante = { navController.navigate("asistencia_estudiante/$idExtraido") },
                onNavigateToCrearComunicado = { navController.navigate("crear_comunicado") },
                onNavigateToVerComunicados = { navController.navigate("ver_comunicados") },
                onNavigateToCalificacionesEstudiante = { navController.navigate("calificaciones_estudiante/$idExtraido") },
                onNavigateToRegistrarCalificacion = { navController.navigate("registrar_calificacion") } // <-- Agregado aquí
            )
        }

        // Nueva ruta para la Asistencia Docente
        composable("asistencia") {
            AsistenciaDocenteScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("asistencia_estudiante/{idUsuario}") { backStackEntry ->
            val idExtraido = backStackEntry.arguments?.getString("idUsuario")?.toIntOrNull() ?: 0

            AsistenciaEstudianteScreen(
                alumnoId = idExtraido, // <-- Le entregamos el ID real a la pantalla
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("crear_comunicado") {
            CrearComunicadoScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("ver_comunicados") {
            ListaComunicadosScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("calificaciones_estudiante/{idUsuario}") { backStackEntry ->
            val idExtraido = backStackEntry.arguments?.getString("idUsuario")?.toIntOrNull() ?: 0
            CalificacionesEstudianteScreen(
                alumnoId = idExtraido,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("registrar_calificacion") {
            RegistrarCalificacionScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
