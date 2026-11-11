
package cl.DSY1105.reservasalas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.DSY1105.reservasalas.data.repository.Repo
import cl.DSY1105.reservasalas.data.room.AppDatabase
import cl.DSY1105.reservasalas.data.room.Reserva
import cl.DSY1105.reservasalas.nativos.Notificaciones
import cl.DSY1105.reservasalas.utils.Bloques
import cl.DSY1105.reservasalas.utils.hoy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repo(AppDatabase.get(app).dao())

    // ---- Estado de sesión ----
    val relator = MutableStateFlow("")
    val emailActual = MutableStateFlow("")
    val esCoordinador = MutableStateFlow(false)
    val sesionInicialRestaurada = MutableStateFlow(false)

    // ---- Filtros ----
    val fecha = MutableStateFlow(hoy())
    val capMin = MutableStateFlow(0)
    val soloProyector = MutableStateFlow(false)

    // ---- Mensajes y carga ----
    val mensaje = MutableStateFlow<String?>(null)
    val error = MutableStateFlow<String?>(null)
    val aviso = MutableStateFlow<String?>(null)
    val cargando = MutableStateFlow(false)

    // ---- Reservas ----
    val reservas = fecha.flatMapLatest { fechaSeleccionada ->
        repo.reservas(fechaSeleccionada)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    // ---- Salas ----
    val salas = combine(
        repo.salas(),
        capMin,
        soloProyector
    ) { lista, capacidad, proyector ->
        lista.filter {
            it.capacidad >= capacidad &&
                    (!proyector || it.proyector)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    // ---- Usuarios ----
    val usuarios = repo.usuarios().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    // ---- Inicialización ----
    init {
        viewModelScope.launch {
            try {
                repo.sembrar(hoy())
            } catch (e: Exception) {
                error.value = "No se pudieron cargar los datos locales."
            }
        }

        viewModelScope.launch {
            try {
                val usuario = repo.restaurarSesion()

                if (usuario != null && usuario.activo) {
                    relator.value = usuario.nombre
                    emailActual.value = usuario.email
                    esCoordinador.value = usuario.esCoordinador
                } else {
                    repo.cerrarSesion()
                    limpiarSesion()
                }
            } catch (e: Exception) {
                limpiarSesion()
            } finally {
                sesionInicialRestaurada.value = true
            }
        }
    }

    // ---- Inicio de sesión ----
    fun iniciarSesion(
        email: String,
        pass: String,
        onOk: () -> Unit
    ) = viewModelScope.launch {

        if (cargando.value) return@launch

        cargando.value = true
        error.value = null
        aviso.value = null

        try {
            val usuario = repo.login(email, pass)

            if (usuario == null || !usuario.activo) {
                repo.cerrarSesion()
                limpiarSesion()
                error.value =
                    "No se pudo iniciar sesión. Revisa tu correo y contraseña."
            } else {
                relator.value = usuario.nombre
                emailActual.value = usuario.email
                esCoordinador.value = usuario.esCoordinador
                onOk()
            }
        } catch (e: Exception) {
            error.value = "Ocurrió un error al iniciar sesión."
        } finally {
            cargando.value = false
        }
    }

    // ---- Registro ----
    fun registrar(
        nombre: String,
        email: String,
        pass: String,
        onOk: () -> Unit
    ) = viewModelScope.launch {

        if (cargando.value) return@launch

        cargando.value = true
        error.value = null
        aviso.value = null

        try {
            if (repo.registrar(nombre, email, pass)) {
                aviso.value = "Cuenta creada. Ahora inicia sesión."
                onOk()
            } else {
                error.value =
                    "No se pudo crear la cuenta. Verifica los datos e inténtalo nuevamente."
            }
        } catch (e: Exception) {
            error.value = "Ocurrió un error al crear la cuenta."
        } finally {
            cargando.value = false
        }
    }

    // ---- Cierre de sesión ----
    fun cerrarSesion() {
        repo.cerrarSesion()
        limpiarSesion()
    }

    private fun limpiarSesion() {
        relator.value = ""
        emailActual.value = ""
        esCoordinador.value = false
        error.value = null
        aviso.value = null
        mensaje.value = null
    }

    // ---- Gestión provisional de roles ----
    fun cambiarRol(email: String, nuevoCoordinador: Boolean) {
        if (!esCoordinador.value || email == emailActual.value) return

        mensaje.value =
            "La gestión de roles estará disponible al conectar el backend."
    }

    // ---- Reservas locales provisionales ----
    fun reservar(
        salaId: Long,
        bloque: Int,
        actividad: String
    ) = viewModelScope.launch {

        try {
            val ok = repo.reservar(
                Reserva(
                    salaId = salaId,
                    fecha = fecha.value,
                    bloque = bloque,
                    relator = relator.value,
                    actividad = actividad
                )
            )

            if (ok) {
                Notificaciones.mostrar(
                    getApplication<Application>(),
                    "Reserva confirmada",
                    "$actividad · ${Bloques.lista[bloque]} · ${fecha.value}"
                )
            }

            mensaje.value = if (ok) {
                "Reserva confirmada"
            } else {
                "Ese bloque ya fue reservado"
            }
        } catch (e: Exception) {
            mensaje.value = "No se pudo realizar la reserva."
        }
    }

    // ---- Cancelación local provisional ----
    fun cancelar(r: Reserva) = viewModelScope.launch {
        try {
            repo.cancelar(r)
            mensaje.value = "Reserva cancelada."
        } catch (e: Exception) {
            mensaje.value = "No se pudo cancelar la reserva."
        }
    }
}
