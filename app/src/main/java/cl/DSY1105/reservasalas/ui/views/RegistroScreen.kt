
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
import cl.DSY1105.reservasalas.utils.esEmailValido
import cl.DSY1105.reservasalas.utils.esPasswordValida

@Composable
fun RegistroScreen(
    vm: AgendaViewModel,
    onVolver: () -> Unit,
    onOk: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    val error by vm.error.collectAsState()
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
            "Crear cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            singleLine = true,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

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
            label = { Text("Contraseña (mín. 8 caracteres)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                vm.registrar(nombre.trim(), email, pass, onOk)
            },
            enabled = nombre.isNotBlank() &&
                    esEmailValido(email) &&
                    esPasswordValida(pass) &&
                    !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Registrando...")
            } else {
                Text("Registrarme")
            }
        }

        TextButton(
            onClick = onVolver,
            enabled = !cargando
        ) {
            Text("Volver")
        }
    }
}
