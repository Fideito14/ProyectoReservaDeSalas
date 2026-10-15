package cl.DSY1105.reservasalas.ui.views

import android.app.DatePickerDialog
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
import java.util.Calendar
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import cl.DSY1105.reservasalas.nativos.Notificaciones

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(vm: AgendaViewModel, onUsuarios: () -> Unit, onSalir: () -> Unit) {
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
    // Pide el permiso de notificaciones al entrar a la agenda (Android 13+)
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 && !Notificaciones.tienePermiso(ctx)) {
            permiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    var destino by remember { mutableStateOf<Pair<Sala, Int>?>(null) } // sala + bloque a reservar
    var actividad by remember { mutableStateOf("") }

    LaunchedEffect(msg) { msg?.let { snack.showSnackbar(it); vm.mensaje.value = null } }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text(if (coord) "Agenda (Coordinador)" else "Agenda") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                actions = {
                    if (coord) TextButton(onClick = onUsuarios) { Text("Usuarios") }
                    TextButton(onClick = onSalir) { Text("Salir") }
                })
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text("Hola, $yo")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    val c = Calendar.getInstance()
                    DatePickerDialog(ctx, { _, y, m, d -> vm.fecha.value = "%04d-%02d-%02d".format(y, m + 1, d) },
                        c[Calendar.YEAR], c[Calendar.MONTH], c[Calendar.DAY_OF_MONTH]).show()
                }) { Text(fecha) }
                OutlinedTextField(if (cap == 0) "" else cap.toString(), { vm.capMin.value = it.toIntOrNull() ?: 0 },
                    label = { Text("Cap. mín.") }, singleLine = true, modifier = Modifier.width(110.dp))
                Switch(proy, { vm.soloProyector.value = it }); Text("Proyector")
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(salas, key = { it.id }) { sala ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(sala.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("Capacidad ${sala.capacidad}" +
                                    (if (sala.proyector) " · Proyector" else "") +
                                    (if (sala.computadores) " · Computadores" else "") +
                                    (if (sala.climatizacion) " · Clima" else ""),
                                style = MaterialTheme.typography.bodySmall, color = Paleta.TextoSec)
                            Bloques.lista.forEachIndexed { i, hora ->
                                val r = reservas.find { it.salaId == sala.id && it.bloque == i }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    if (r == null) {
                                        Text("$hora · Disponible", color = Paleta.ExitoTexto)
                                        TextButton(onClick = { destino = sala to i; actividad = "" }) { Text("Reservar") }
                                    } else {
                                        Text("$hora · ${r.actividad} (${r.relator})",
                                            color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                                        if (coord || r.relator == yo)
                                            TextButton(onClick = { vm.cancelar(r) }) { Text("Cancelar") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    destino?.let { (sala, bloque) ->
        AlertDialog(
            onDismissRequest = { destino = null },
            title = { Text("${sala.nombre} · ${Bloques.lista[bloque]}") },
            text = { OutlinedTextField(actividad, { actividad = it }, label = { Text("Curso / actividad") }) },
            confirmButton = {
                TextButton(enabled = actividad.isNotBlank(),
                    onClick = { vm.reservar(sala.id, bloque, actividad.trim()); destino = null }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { destino = null }) { Text("Cerrar") } })
    }
}