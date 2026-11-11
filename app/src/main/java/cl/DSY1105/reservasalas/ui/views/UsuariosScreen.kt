
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
fun UsuariosScreen(
    vm: AgendaViewModel,
    onVolver: () -> Unit
) {
    val usuarios by vm.usuarios.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usuarios") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    TextButton(onClick = onVolver) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
        ) {
            Text(
                "Los perfiles y roles son provisionales hasta conectar el backend.",
                style = MaterialTheme.typography.bodySmall,
                color = Paleta.TextoSec
            )

            Spacer(Modifier.height(12.dp))

            if (usuarios.isEmpty()) {
                Text("No hay usuarios guardados localmente.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(usuarios, key = { it.uid }) { usuario ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        usuario.nombre,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        usuario.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Paleta.TextoSec
                                    )

                                    Text(
                                        if (usuario.esCoordinador) {
                                            "Coordinador"
                                        } else {
                                            "Relator"
                                        },
                                        color = if (usuario.esCoordinador) {
                                            Paleta.ExitoTexto
                                        } else {
                                            Paleta.TextoSec
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
