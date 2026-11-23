
package cl.DSY1105.reservasalas.viewmodel

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.DSY1105.reservasalas.data.api.RetrofitClient
import cl.DSY1105.reservasalas.data.repository.AppRepository
import cl.DSY1105.reservasalas.data.room.AppDatabase
import cl.DSY1105.reservasalas.data.room.NetworkMonitor
import cl.DSY1105.reservasalas.data.room.Reserva
import cl.DSY1105.reservasalas.data.room.SessionManager
import cl.DSY1105.reservasalas.nativos.Notificaciones
import cl.DSY1105.reservasalas.utils.Bloques
import cl.DSY1105.reservasalas.utils.hoy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModel(app: Application) : AndroidViewModel(app) {

    private val sessionManager = SessionManager(app)
    private val networkMonitor = NetworkMonitor(app)
    private val dao = AppDatabase.get(app).dao()

    private val repo = AppRepository(
        api = RetrofitClient.create(sessionManager),
        dao = dao,
        sessionManager = sessionManager,
        networkMonitor = networkMonitor
    )

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

    val reservas = combine(
        repo.reservas,
        fecha
    ) { lista, fechaSeleccionada ->
        lista.filter {
            it.fecha == fechaSeleccionada
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    // ---- Salas ----

    val salas = combine(
        repo.salas,
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

    val usuarios = repo.usuarios.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    // ---- Inicialización ----

    init {
        viewModelScope.launch {
            try {
                val token = sessionManager.tokenFlow.first()

                Log.d(
                    "SESSION_DEBUG",
                    "Token guardado: ${token.isNotBlank()}"
                )

                if (token.isNotBlank()) {

                    if (networkMonitor.isOnline()) {

                        repo.obtenerPerfil().fold(
                            onSuccess = { usuario ->
                                relator.value = usuario.nombre
                                emailActual.value = usuario.email
                                esCoordinador.value = usuario.esCoordinador
                            },
                            onFailure = { exception ->
                                Log.e(
                                    "SESSION_DEBUG",
                                    "No se pudo recuperar el perfil remoto",
                                    exception
                                )

                                restaurarPerfilLocal(token)
                            }
                        )

                    } else {
                        restaurarPerfilLocal(token)

                        aviso.value =
                            "Sin conexión. Puedes consultar los datos guardados."
                    }

                } else {
                    Log.w(
                        "SESSION_DEBUG",
                        "No existe un token guardado"
                    )
                }

            } catch (e: Exception) {
                Log.e(
                    "SESSION_DEBUG",
                    "Error durante la inicialización",
                    e
                )

                error.value = "No se pudo restaurar la sesión."

            } finally {
                sesionInicialRestaurada.value = true
            }
        }
    }

    // ---- Recuperar perfil desde Room ----

    private suspend fun restaurarPerfilLocal(token: String) {
        try {
            val correo = obtenerCorreoDelToken(token)

            if (correo.isNullOrBlank()) {
                aviso.value =
                    "No se pudo identificar el usuario guardado localmente."
                return
            }

            val usuarioLocal = dao.usuarios()
                .first()
                .find {
                    it.email.equals(correo, ignoreCase = true)
                }

            if (usuarioLocal != null) {
                relator.value = usuarioLocal.nombre
                emailActual.value = usuarioLocal.email
                esCoordinador.value = usuarioLocal.esCoordinador
            } else {
                aviso.value =
                    "No hay un perfil guardado localmente para esta sesión."
            }

        } catch (e: Exception) {
            aviso.value =
                "No se pudo recuperar el perfil guardado localmente."
        }
    }

    // ---- Obtener correo desde JWT ----

    private fun obtenerCorreoDelToken(token: String): String? {
        return try {
            val tokenLimpio = token.removePrefix("Bearer ").trim()
            val partes = tokenLimpio.split(".")

            if (partes.size < 2) {
                return null
            }

            val payload = Base64.decode(
                partes[1],
                Base64.URL_SAFE or
                        Base64.NO_WRAP or
                        Base64.NO_PADDING
            )

            val json = JSONObject(String(payload, Charsets.UTF_8))

            json.optString("sub")
                .takeIf { it.isNotBlank() }

        } catch (e: Exception) {
            null
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
            repo.login(email, pass).fold(

                onSuccess = { usuario ->
                    relator.value = usuario.nombre
                    emailActual.value = usuario.email
                    esCoordinador.value = usuario.esCoordinador

                    // Ya no se intenta sincronizar salas.
                    onOk()
                },

                onFailure = { exception ->
                    error.value = exception.message
                        ?: "No se pudo iniciar sesión."
                }
            )

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
            repo.registrar(nombre, email, pass).fold(

                onSuccess = { respuesta ->
                    aviso.value = respuesta
                    onOk()
                },

                onFailure = { exception ->
                    error.value = exception.message
                        ?: "No se pudo crear la cuenta."
                }
            )

        } catch (e: Exception) {
            error.value = "Ocurrió un error al crear la cuenta."
        } finally {
            cargando.value = false
        }
    }

    // ---- Cierre de sesión ----

    fun cerrarSesion() {
        viewModelScope.launch {
            repo.cerrarSesion()
            limpiarSesion()
        }
    }

    private fun limpiarSesion() {
        relator.value = ""
        emailActual.value = ""
        esCoordinador.value = false
        error.value = null
        aviso.value = null
        mensaje.value = null
    }

    // ---- Sincronización de usuarios ----

    fun cargarUsuarios() {
        if (!esCoordinador.value) return

        viewModelScope.launch {
            repo.sincronizarUsuarios().fold(
                onSuccess = {
                    aviso.value = null
                },
                onFailure = { exception ->
                    aviso.value = exception.message
                        ?: "No se pudieron actualizar los usuarios."
                }
            )
        }
    }

    // ---- Gestión de roles ----

    fun cambiarRol(email: String, nuevoCoordinador: Boolean) {
        if (!esCoordinador.value || email == emailActual.value) return

        mensaje.value =
            "La gestión de roles no está conectada todavía."
    }

    // ---- Reservas locales por grupos de bloques ----

    fun reservar(
        salaId: Long,
        bloque: Int,
        actividad: String
    ) {
        reservarBloques(salaId, listOf(bloque), actividad)
    }

    fun reservarBloques(
        salaId: Long,
        bloques: List<Int>,
        actividad: String
    ) = viewModelScope.launch {

        try {
            // No permitir crear reservas sin conexión.
            if (!networkMonitor.isOnline()) {
                mensaje.value =
                    "Necesitas conexión a internet para crear una reserva."
                return@launch
            }

            if (relator.value.isBlank()) {
                mensaje.value = "Debes iniciar sesión para reservar."
                return@launch
            }

            val bloquesOrdenados = bloques.distinct().sorted()

            if (bloquesOrdenados.isEmpty()) {
                mensaje.value = "Selecciona al menos un bloque."
                return@launch
            }

            if (bloquesOrdenados.any { it !in Bloques.lista.indices }) {
                mensaje.value = "Seleccionaste un bloque inválido."
                return@launch
            }

            if (bloquesOrdenados.zipWithNext().any { (a, b) ->
                    b != a + 1
                }) {
                mensaje.value = "Selecciona bloques consecutivos."
                return@launch
            }

            if (actividad.isBlank()) {
                mensaje.value = "Debes ingresar una actividad."
                return@launch
            }

            val grupoId = UUID.randomUUID().toString()

            val reservasGrupo = bloquesOrdenados.map { bloque ->
                Reserva(
                    salaId = salaId,
                    fecha = fecha.value,
                    bloque = bloque,
                    relator = relator.value,
                    actividad = actividad.trim(),
                    grupoId = grupoId
                )
            }

            dao.insertarGrupo(reservasGrupo)

            val horarios = bloquesOrdenados.joinToString(", ") {
                Bloques.lista[it]
            }

            Notificaciones.mostrar(
                getApplication<Application>(),
                "Reserva guardada",
                "$actividad · $horarios · ${fecha.value}"
            )

            mensaje.value = "Reserva guardada correctamente."

        } catch (e: Exception) {
            mensaje.value =
                "No se pudo guardar la reserva. Uno o más bloques podrían estar ocupados."
        }
    }

    // ---- Cancelación de reservas ----

    fun cancelar(r: Reserva) = viewModelScope.launch {

        try {
            // No permitir cancelar reservas sin conexión.
            if (!networkMonitor.isOnline()) {
                mensaje.value =
                    "Necesitas conexión a internet para cancelar una reserva."
                return@launch
            }

            dao.eliminarReserva(r.id)

            mensaje.value = "Bloque cancelado correctamente."

        } catch (e: Exception) {
            mensaje.value = "No se pudo cancelar el bloque."
        }
    }
}
