package cl.DSY1105.reservasalas.data

import android.database.sqlite.SQLiteConstraintException

// Offline-first: la UI siempre lee de Room; la API sincroniza cuando hay conexión.
class Repo(private val dao: AppDao) {
    fun salas() = dao.salas()
    fun reservas(fecha: String) = dao.reservas(fecha)

    suspend fun sincronizar(fecha: String) {
        try { dao.guardarSalas(Api.service.salas()); dao.guardarReservas(Api.service.reservas(fecha)) }
        catch (_: Exception) { /* sin servidor: se trabaja con datos locales */ }
    }

    suspend fun reservar(r: Reserva): Boolean = try {
        val id = dao.insertar(r)
        try { Api.service.reservar(r.copy(id = id)) } catch (_: Exception) { }
        true
    } catch (_: SQLiteConstraintException) { false }

    suspend fun cancelar(r: Reserva) {
        dao.eliminar(r)
        try { Api.service.cancelar(r.id) } catch (_: Exception) { }
    }

    // Dataset ficticio inicial (catálogo de salas + 2 reservas de ejemplo para hoy)
    suspend fun sembrar(hoy: String) {
        if (dao.contarSalas() > 0) return
        dao.guardarSalas(listOf(
            Sala(1, "Sala A101", 30, true, false, true),
            Sala(2, "Lab Computación 1", 20, true, true, true),
            Sala(3, "Sala B201", 40, false, false, false),
            Sala(4, "Multiuso Auditorio", 80, true, false, true)))
        dao.guardarReservas(listOf(
            Reserva(salaId = 1, fecha = hoy, bloque = 0, relator = "Ana Pérez", actividad = "Excel Avanzado"),
            Reserva(salaId = 2, fecha = hoy, bloque = 1, relator = "Luis Soto", actividad = "Python Básico")))
    }
}
