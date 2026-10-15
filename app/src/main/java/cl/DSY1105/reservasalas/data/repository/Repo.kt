package cl.DSY1105.reservasalas.data.repository

import android.database.sqlite.SQLiteConstraintException
import cl.DSY1105.reservasalas.data.room.AppDao
import cl.DSY1105.reservasalas.data.room.Reserva
import cl.DSY1105.reservasalas.data.room.Sala
import cl.DSY1105.reservasalas.data.room.Usuario
import cl.DSY1105.reservasalas.utils.Seguridad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repo(private val dao: AppDao) {
    fun salas() = dao.salas()
    fun reservas(fecha: String) = dao.reservas(fecha)

    suspend fun reservar(r: Reserva): Boolean = try {
        dao.insertar(r)
        true
    } catch (_: SQLiteConstraintException) { false } // bloque ya reservado

    suspend fun cancelar(r: Reserva) = dao.eliminar(r)

    // ---- Usuarios ----
    fun usuarios() = dao.usuarios()

    suspend fun cambiarRol(email: String, esCoordinador: Boolean) = dao.cambiarRol(email, esCoordinador)

    suspend fun registrar(nombre: String, email: String, password: String): Boolean =
        withContext(Dispatchers.Default) {
            val sal = Seguridad.nuevaSal()
            try {
                dao.insertarUsuario(Usuario(email.trim().lowercase(), nombre, sal,
                    Seguridad.hash(password, sal), false)) // desde la app siempre es Relator
                true
            } catch (_: SQLiteConstraintException) { false } // correo ya registrado
        }

    suspend fun login(email: String, password: String): Usuario? = withContext(Dispatchers.Default) {
        val u = dao.buscarUsuario(email.trim().lowercase()) ?: return@withContext null
        if (Seguridad.verificar(password, u.salt, u.hash)) u else null
    }

    // Coordinador inicial de prueba (dato ficticio): coordinador@therionlabs.cl / Coord1234
    suspend fun sembrarCoordinador() {
        if (dao.buscarUsuario("coordinador@therionlabs.cl") != null) return
        val sal = Seguridad.nuevaSal()
        dao.insertarUsuario(Usuario("coordinador@therionlabs.cl", "Coordinador",
            sal, Seguridad.hash("Coord1234", sal), true))
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