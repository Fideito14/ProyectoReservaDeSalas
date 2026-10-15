package cl.DSY1105.reservasalas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.DSY1105.reservasalas.data.repository.Repo
import cl.DSY1105.reservasalas.data.room.AppDatabase
import cl.DSY1105.reservasalas.data.room.Reserva
import cl.DSY1105.reservasalas.utils.hoy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import cl.DSY1105.reservasalas.nativos.Notificaciones
import cl.DSY1105.reservasalas.utils.Bloques

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repo(AppDatabase.get(app).dao())

    // Estado de sesión y filtros (sobrevive a rotaciones)
    val relator = MutableStateFlow("")
    val emailActual = MutableStateFlow("")
    val esCoordinador = MutableStateFlow(false)
    val fecha = MutableStateFlow(hoy())
    val capMin = MutableStateFlow(0)
    val soloProyector = MutableStateFlow(false)
    val mensaje = MutableStateFlow<String?>(null)
    val error = MutableStateFlow<String?>(null)
    val aviso = MutableStateFlow<String?>(null)

    val reservas = fecha.flatMapLatest { repo.reservas(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val salas = combine(repo.salas(), capMin, soloProyector) { s, cap, proy ->
        s.filter { it.capacidad >= cap && (!proy || it.proyector) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val usuarios = repo.usuarios()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch { repo.sembrarCoordinador() }
        viewModelScope.launch { repo.sembrar(hoy()) }
    }

    fun iniciarSesion(email: String, pass: String, onOk: () -> Unit) = viewModelScope.launch {
        val u = repo.login(email, pass)
        if (u == null) error.value = "Correo o contraseña incorrectos"
        else {
            relator.value = u.nombre
            emailActual.value = u.email
            esCoordinador.value = u.esCoordinador
            error.value = null
            aviso.value = null
            onOk()
        }
    }

    fun registrar(nombre: String, email: String, pass: String, onOk: () -> Unit) = viewModelScope.launch {
        if (repo.registrar(nombre, email, pass)) {
            aviso.value = "Cuenta creada. Ahora inicia sesión"
            onOk()
        } else error.value = "Ese correo ya está registrado"
    }

    fun cerrarSesion() { relator.value = ""; emailActual.value = ""; esCoordinador.value = false }

    // Solo un Coordinador puede cambiar roles, y nadie puede cambiarse el suyo
    fun cambiarRol(email: String, nuevoCoordinador: Boolean) = viewModelScope.launch {
        if (!esCoordinador.value || email == emailActual.value) return@launch
        repo.cambiarRol(email, nuevoCoordinador)
        mensaje.value = if (nuevoCoordinador) "Ahora es Coordinador" else "Ahora es Relator"
    }

    fun reservar(salaId: Long, bloque: Int, actividad: String) = viewModelScope.launch {
        val ok = repo.reservar(Reserva(salaId = salaId, fecha = fecha.value, bloque = bloque,
            relator = relator.value, actividad = actividad))
        if (ok) {
            Notificaciones.mostrar(getApplication<Application>(), "Reserva confirmada",
                "$actividad · ${Bloques.lista[bloque]} · ${fecha.value}")
        }
        mensaje.value = if (ok) "Reserva confirmada" else "Ese bloque ya fue reservado"
    }

    fun cancelar(r: Reserva) = viewModelScope.launch { repo.cancelar(r) }
}