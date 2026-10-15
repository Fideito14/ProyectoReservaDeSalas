package cl.DSY1105.reservasalas.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.DSY1105.reservasalas.viewmodel.AgendaViewModel
import cl.DSY1105.reservasalas.ui.theme.Paleta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(vm: AgendaViewModel, onVolver: () -> Unit) {
    val usuarios by vm.usuarios.collectAsState()
    val yo by vm.emailActual.collectAsState()
    val msg by vm.mensaje.collectAsState()
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(msg) { msg?.let { snack.showSnackbar(it); vm.mensaje.value = null } }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text("Usuarios") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                actions = { TextButton(onClick = onVolver) { Text("Volver") } })
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(usuarios, key = { it.email }) { u ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(u.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(u.email, style = MaterialTheme.typography.bodySmall, color = Paleta.TextoSec)
                            Text(if (u.esCoordinador) "Coordinador" else "Relator",
                                color = if (u.esCoordinador) Paleta.ExitoTexto else Paleta.TextoSec)
                        }
                        // Nadie puede cambiarse el rol a sí mismo
                        if (u.email != yo) {
                            TextButton(onClick = { vm.cambiarRol(u.email, !u.esCoordinador) }) {
                                Text(if (u.esCoordinador) "Quitar rol" else "Hacer Coordinador")
                            }
                        }
                    }
                }
            }
        }
    }
}