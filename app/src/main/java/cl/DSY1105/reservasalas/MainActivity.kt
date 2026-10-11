package cl.DSY1105.reservasalas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.DSY1105.reservasalas.ui.*
import cl.DSY1105.reservasalas.ui.theme.ReservaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReservaTheme {
                val nav = rememberNavController()
                val vm: AgendaViewModel = viewModel()
                NavHost(nav, startDestination = "login") {
                    composable("login") {
                        LoginScreen(vm) { nav.navigate("agenda") { popUpTo("login") { inclusive = true } } }
                    }
                    composable("agenda") {
                        AgendaScreen(vm) { nav.navigate("login") { popUpTo("agenda") { inclusive = true } } }
                    }
                }
            }
        }
    }
}
