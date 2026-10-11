package cl.DSY1105.reservasalas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores tomados de mockups_reserva_salas.html
object Paleta {
    val Fondo = Color(0xFFF4F5F7)      // --bg-page
    val Tarjeta = Color(0xFFEEF0F3)    // --surface-1
    val Borde = Color(0xFFE0E2E6)      // --border
    val Texto = Color(0xFF1A1A1A)      // --text-primary
    val TextoSec = Color(0xFF6B6F76)   // --text-secondary
    val ExitoTexto = Color(0xFF0F6E56) // --text-success
    val ExitoFondo = Color(0xFFE1F5EE) // --bg-success
    val PeligroTexto = Color(0xFFA32D2D) // --text-danger
    val PeligroFondo = Color(0xFFFCEBEB) // --bg-danger
}

@Composable
fun ReservaTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = lightColorScheme(
        primary = Paleta.Texto, onPrimary = Color.White,
        primaryContainer = Paleta.Tarjeta, onPrimaryContainer = Paleta.Texto,
        secondaryContainer = Paleta.ExitoFondo, onSecondaryContainer = Paleta.ExitoTexto,
        background = Paleta.Fondo, onBackground = Paleta.Texto,
        surface = Color.White, onSurface = Paleta.Texto, onSurfaceVariant = Paleta.TextoSec,
        surfaceVariant = Paleta.Tarjeta, surfaceContainerHighest = Paleta.Tarjeta,
        error = Paleta.PeligroTexto, errorContainer = Paleta.PeligroFondo,
        outline = Paleta.Borde, outlineVariant = Paleta.Borde),
    content = content)
