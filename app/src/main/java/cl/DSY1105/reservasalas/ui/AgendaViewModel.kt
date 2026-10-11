package cl.DSY1105.reservasalas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.DSY1105.reservasalas.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

fun hoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repo(AppDatabase.get(app).dao())

    // Estado de sesión y filtros (sobrevive a rotaciones)
    val relator = MutableStateFlow("")
    val esCoordinador = MutableStateFlow(false)
    val fecha = MutableStateFlow(hoy())
    val capMin = MutableStateFlow(0)
    val soloProyector = MutableStateFlow(false)
    val mensaje = MutableStateFlow<String?>(null)

    val reservas = fecha.flatMapLatest { repo.reservas(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val salas = combine(repo.salas(), capMin, soloProyector) { s, cap, proy ->
        s.filter { it.capacidad >= cap && (!proy || it.proyector) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch { repo.sembrar(hoy()) }
        viewModelScope.launch { fecha.collect { repo.sincronizar(it) } }
    }

    fun iniciar(nombre: String, coordinador: Boolean) { relator.value = nombre; esCoordinador.value = coordinador }

    fun reservar(salaId: Long, bloque: Int, actividad: String) = viewModelScope.launch {
        val ok = repo.reservar(Reserva(salaId = salaId, fecha = fecha.value, bloque = bloque,
            relator = relator.value, actividad = actividad))
        mensaje.value = if (ok) "Reserva confirmada" else "Ese bloque ya fue reservado"
    }

    fun cancelar(r: Reserva) = viewModelScope.launch { repo.cancelar(r) }
}
