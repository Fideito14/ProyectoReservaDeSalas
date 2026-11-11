
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
fun LoginScreen(
    vm: AgendaViewModel,
    onRegistro: () -> Unit,
    onOk: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    val error by vm.error.collectAsState()
    val aviso by vm.aviso.collectAsState()
    val cargando by vm.cargando.collectAsState()

    LaunchedEffect(Unit) {
        vm.error.value = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Reserva de Salas",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        )

        aviso?.let {
            Text(
                text = it,
                color = Paleta.ExitoTexto
            )
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                vm.iniciarSesion(email, pass, onOk)
            },
            enabled = esEmailValido(email) && pass.isNotEmpty() && !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Ingresando...")
            } else {
                Text("Ingresar")
            }
        }

        TextButton(
            onClick = onRegistro,
            enabled = !cargando
        ) {
            Text("Crear cuenta")
        }
    }
}
