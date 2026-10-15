package cl.DSY1105.reservasalas.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.DSY1105.reservasalas.viewmodel.AgendaViewModel
import cl.DSY1105.reservasalas.ui.theme.Paleta
import cl.DSY1105.reservasalas.utils.esEmailValido

@Composable
fun LoginScreen(vm: AgendaViewModel, onRegistro: () -> Unit, onOk: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val error by vm.error.collectAsState()
    val aviso by vm.aviso.collectAsState()
    LaunchedEffect(Unit) { vm.error.value = null }

    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Reserva de Salas", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Correo") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(pass, { pass = it }, label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth())
        aviso?.let { Text(it, color = Paleta.ExitoTexto) }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { vm.iniciarSesion(email, pass, onOk) },
            enabled = esEmailValido(email) && pass.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()) { Text("Ingresar") }
        TextButton(onClick = onRegistro) { Text("Crear cuenta") }
    }
}