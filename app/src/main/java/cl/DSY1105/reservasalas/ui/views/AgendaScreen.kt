
package cl.DSY1105.reservasalas.ui.views

import android.app.DatePickerDialog
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.DSY1105.reservasalas.data.room.Sala
import cl.DSY1105.reservasalas.viewmodel.AgendaViewModel
import cl.DSY1105.reservasalas.ui.theme.Paleta
import cl.DSY1105.reservasalas.utils.Bloques
import cl.DSY1105.reservasalas.nativos.Notificaciones
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    vm: AgendaViewModel,
    onUsuarios: () -> Unit,
    onSalir: () -> Unit
) {
    val ctx = LocalContext.current

    val salas by vm.salas.collectAsState()
    val reservas by vm.reservas.collectAsState()
    val fecha by vm.fecha.collectAsState()
    val cap by vm.capMin.collectAsState()
    val proy by vm.soloProyector.collectAsState()
    val yo by vm.relator.collectAsState()
    val coord by vm.esCoordinador.collectAsState()
    val msg by vm.mensaje.collectAsState()

    val snack = remember { SnackbarHostState() }

    // Permiso de notificaciones Android 13+
    val permiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            !Notificaciones.tienePermiso(ctx)
        ) {
            permiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Estado de selección de bloques
    var salaSeleccionada by remember {
        mutableStateOf<Sala?>(null)
    }

    var bloquesSeleccionados by remember {
        mutableStateOf<List<Int>>(emptyList())
    }

    var mostrarDialogo by remember {
        mutableStateOf(false)
    }

    var actividad by remember {
        mutableStateOf("")
    }

    // Mostrar mensajes mediante Snackbar
    LaunchedEffect(msg) {
        msg?.let {
            snack.showSnackbar(it)
            vm.mensaje.value = null
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snack)
        },

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (coord) "Agenda (Coordinador)"
                        else "Agenda"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (coord) {
                        TextButton(onClick = onUsuarios) {
                            Text("Usuarios")
                        }
                    }

                    TextButton(onClick = onSalir) {
                        Text("Salir")
                    }
                }
            )
        }
    ) { pad ->

        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
        ) {

            Text(
                "Hola, $yo",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            // Filtros
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = {
                        val c = Calendar.getInstance()

                        DatePickerDialog(
                            ctx,
                            { _, y, m, d ->
                                vm.fecha.value =
                                    "%04d-%02d-%02d".format(
                                        y,
                                        m + 1,
                                        d
                                    )
                            },
                            c[Calendar.YEAR],
                            c[Calendar.MONTH],
                            c[Calendar.DAY_OF_MONTH]
                        ).show()
                    }
                ) {
                    Text(fecha)
                }

                OutlinedTextField(
                    value = if (cap == 0) "" else cap.toString(),
                    onValueChange = {
                        vm.capMin.value = it.toIntOrNull() ?: 0
                    },
                    label = {
                        Text("Cap. mín.")
                    },
                    singleLine = true,
                    modifier = Modifier.width(110.dp)
                )

                Switch(
                    checked = proy,
                    onCheckedChange = {
                        vm.soloProyector.value = it
                    }
                )

                Text("Proyector")
            }

            Spacer(Modifier.height(12.dp))

            // Leyenda
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "● Disponible",
                    color = Paleta.ExitoTexto
                )

                Text(
                    "● Ocupado",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(8.dp))

            // Lista de salas
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(
                    salas,
                    key = { it.id }
                ) { sala ->

                    Card(
                        Modifier.fillMaxWidth()
                    ) {

                        Column(
                            Modifier.padding(12.dp)
                        ) {

                            Text(
                                sala.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                "Capacidad ${sala.capacidad}" +
                                        (if (sala.proyector) " · Proyector" else "") +
                                        (if (sala.computadores) " · Computadores" else "") +
                                        (if (sala.climatizacion) " · Clima" else ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = Paleta.TextoSec
                            )

                            Spacer(Modifier.height(8.dp))

                            // Horarios de la sala
                            Bloques.lista.forEachIndexed { i, hora ->

                                val r = reservas.find {
                                    it.salaId == sala.id &&
                                            it.bloque == i
                                }

                                val seleccionado =
                                    salaSeleccionada?.id == sala.id &&
                                            bloquesSeleccionados.contains(i)

                                val puedeSeleccionar =
                                    salaSeleccionada == null ||
                                            salaSeleccionada?.id == sala.id &&
                                            (
                                                    bloquesSeleccionados.isEmpty() ||
                                                            i == bloquesSeleccionados.last() + 1 ||
                                                            seleccionado
                                                    )

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    if (r == null) {

                                        Text(
                                            "$hora · Disponible",
                                            color = if (seleccionado) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                Paleta.ExitoTexto
                                            },
                                            modifier = Modifier.weight(1f)
                                        )

                                        FilterChip(
                                            selected = seleccionado,
                                            enabled = puedeSeleccionar,
                                            onClick = {

                                                if (seleccionado) {

                                                    if (
                                                        i == bloquesSeleccionados.last()
                                                    ) {
                                                        bloquesSeleccionados =
                                                            bloquesSeleccionados.dropLast(1)

                                                        if (
                                                            bloquesSeleccionados.isEmpty()
                                                        ) {
                                                            salaSeleccionada = null
                                                        }
                                                    }

                                                } else {

                                                    if (
                                                        salaSeleccionada?.id != sala.id
                                                    ) {
                                                        salaSeleccionada = sala
                                                        bloquesSeleccionados = listOf(i)
                                                    } else {
                                                        bloquesSeleccionados =
                                                            bloquesSeleccionados + i
                                                    }
                                                }
                                            },
                                            label = {
                                                Text(
                                                    if (seleccionado) {
                                                        "Seleccionado"
                                                    } else {
                                                        "Seleccionar"
                                                    }
                                                )
                                            }
                                        )

                                    } else {

                                        Text(
                                            "$hora · ${r.actividad} (${r.relator})",
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (coord || r.relator == yo) {

                                            TextButton(
                                                onClick = {
                                                    vm.cancelar(r)
                                                }
                                            ) {
                                                Text("Cancelar")
                                            }
                                        }
                                    }
                                }
                            }

                            // Acciones de selección
                            if (
                                salaSeleccionada?.id == sala.id &&
                                bloquesSeleccionados.isNotEmpty()
                            ) {

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    "${bloquesSeleccionados.size} bloque(s) seleccionado(s)",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    TextButton(
                                        onClick = {
                                            salaSeleccionada = null
                                            bloquesSeleccionados = emptyList()
                                        }
                                    ) {
                                        Text("Limpiar")
                                    }

                                    Button(
                                        onClick = {
                                            actividad = ""
                                            mostrarDialogo = true
                                        }
                                    ) {
                                        Text("Reservar seleccionados")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación
    if (
        mostrarDialogo &&
        salaSeleccionada != null
    ) {

        val sala = salaSeleccionada!!

        AlertDialog(
            onDismissRequest = {
                mostrarDialogo = false
            },

            title = {
                Text("Confirmar reserva")
            },

            text = {
                Column {

                    Text(
                        sala.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    Text("Fecha: $fecha")

                    Spacer(Modifier.height(8.dp))

                    Text("Bloques seleccionados:")

                    bloquesSeleccionados.forEach { indice ->
                        Text("• ${Bloques.lista[indice]}")
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = actividad,
                        onValueChange = {
                            actividad = it
                        },
                        label = {
                            Text("Curso / actividad")
                        },
                        singleLine = true
                    )
                }
            },

            confirmButton = {
                TextButton(
                    enabled = actividad.isNotBlank() &&
                            bloquesSeleccionados.isNotEmpty(),
                    onClick = {

                        vm.reservarBloques(
                            salaId = sala.id,
                            bloques = bloquesSeleccionados,
                            actividad = actividad.trim()
                        )

                        mostrarDialogo = false
                        salaSeleccionada = null
                        bloquesSeleccionados = emptyList()
                        actividad = ""
                    }
                ) {
                    Text("Confirmar")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogo = false
                    }
                ) {
                    Text("Volver")
                }
            }
        )
    }
}
