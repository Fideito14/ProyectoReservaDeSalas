package cl.DSY1105.reservasalas.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cl.DSY1105.reservasalas.data.AppState
import cl.DSY1105.reservasalas.ui.AgendaScreen
import cl.DSY1105.reservasalas.ui.LoginScreen

@Composable
fun AppNavigation(navHostController: NavHostController, appState: AppState){
    NavHost(navController = navHostController, startDestination = "login"){

    }
}