
package cl.DSY1105.reservasalas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.DSY1105.reservasalas.viewmodel.AgendaViewModel
import cl.DSY1105.reservasalas.ui.views.AgendaScreen
import cl.DSY1105.reservasalas.ui.views.LoginScreen
import cl.DSY1105.reservasalas.ui.views.RegistroScreen
import cl.DSY1105.reservasalas.ui.views.UsuariosScreen

@Composable
fun AppNavigation(
    navHostController: NavHostController,
    vm: AgendaViewModel
) {
    val sesionRestaurada by vm.sesionInicialRestaurada.collectAsState()

    LaunchedEffect(sesionRestaurada) {
        if (sesionRestaurada && vm.relator.value.isNotBlank()) {
            navHostController.navigate("agenda") {
                popUpTo("login") {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navHostController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                vm,
                onRegistro = {
                    navHostController.navigate("registro")
                },
                onOk = {
                    navHostController.navigate("agenda") {
                        popUpTo("login") {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("registro") {
            RegistroScreen(
                vm,
                onVolver = {
                    navHostController.popBackStack()
                },
                onOk = {
                    navHostController.popBackStack()
                }
            )
        }

        composable("agenda") {
            AgendaScreen(
                vm,
                onUsuarios = {
                    navHostController.navigate("usuarios")
                },
                onSalir = {
                    vm.cerrarSesion()
                    navHostController.navigate("login") {
                        popUpTo("agenda") {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("usuarios") {
            UsuariosScreen(
                vm,
                onVolver = {
                    navHostController.popBackStack()
                }
            )
        }
    }
}
