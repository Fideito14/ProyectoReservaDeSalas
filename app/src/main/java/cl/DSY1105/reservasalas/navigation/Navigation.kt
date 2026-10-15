package cl.DSY1105.reservasalas.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.DSY1105.reservasalas.viewmodel.AgendaViewModel
import cl.DSY1105.reservasalas.ui.views.AgendaScreen
import cl.DSY1105.reservasalas.ui.views.LoginScreen
import cl.DSY1105.reservasalas.ui.views.RegistroScreen
import cl.DSY1105.reservasalas.ui.views.UsuariosScreen

@Composable
fun AppNavigation(navHostController: NavHostController, vm: AgendaViewModel) {
    NavHost(navController = navHostController, startDestination = "login") {
        composable("login") {
            LoginScreen(vm,
                onRegistro = { navHostController.navigate("registro") },
                onOk = { navHostController.navigate("agenda") { popUpTo("login") { inclusive = true } } })
        }
        composable("registro") {
            RegistroScreen(vm,
                onVolver = { navHostController.popBackStack() },
                onOk = { navHostController.popBackStack() })
        }
        composable("agenda") {
            AgendaScreen(vm,
                onUsuarios = { navHostController.navigate("usuarios") },
                onSalir = {
                    vm.cerrarSesion()
                    navHostController.navigate("login") { popUpTo("agenda") { inclusive = true } }
                })
        }
        composable("usuarios") {
            UsuariosScreen(vm, onVolver = { navHostController.popBackStack() })
        }
    }
}