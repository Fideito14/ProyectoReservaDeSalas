
package cl.DSY1105.reservasalas.data.repository

import android.database.sqlite.SQLiteConstraintException
import cl.DSY1105.reservasalas.data.room.AppDao
import cl.DSY1105.reservasalas.data.room.Reserva
import cl.DSY1105.reservasalas.data.room.Sala
import cl.DSY1105.reservasalas.data.room.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private suspend fun <T> Task<T>.awaitResult(): T =
    suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(
                    task.exception ?: Exception("Error en la operación")
                )
            }
        }
    }

class Repo(
    private val dao: AppDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // ---- Salas y reservas locales (temporalmente) ----

    fun salas() = dao.salas()

    fun reservas(fecha: String) = dao.reservas(fecha)

    suspend fun reservarGrupo(reservas: List<Reserva>): Boolean {
        if (reservas.isEmpty()) return false

        return try {
            dao.insertarGrupo(reservas)
            true
        } catch (_: SQLiteConstraintException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun cancelarReserva(reservaId: Long) {
        dao.eliminarReserva(reservaId)
    }


    // ---- Usuarios ----

    fun usuarios() = dao.usuarios()

    // El cambio de roles se implementará mediante Spring Boot.
    suspend fun cambiarRol(email: String, esCoordinador: Boolean) {
        // No modificar roles directamente en Room.
    }

    // ---- Registro con Firebase Authentication ----

    suspend fun registrar(
        nombre: String,
        email: String,
        password: String
    ): Boolean {

        var cuentaCreada: FirebaseUser? = null

        return try {
            val correo = email.trim().lowercase()

            val resultado = auth.createUserWithEmailAndPassword(
                correo,
                password
            ).awaitResult()

            val firebaseUser = resultado.user
                ?: throw Exception("No se pudo obtener el usuario creado")

            cuentaCreada = firebaseUser

            val perfil = UserProfileChangeRequest.Builder()
                .setDisplayName(nombre.trim())
                .build()

            firebaseUser.updateProfile(perfil).awaitResult()

            dao.guardarUsuario(
                Usuario(
                    uid = firebaseUser.uid,
                    email = correo,
                    nombre = nombre.trim(),
                    rol = "TRABAJADOR_RELATOR",
                    activo = true
                )
            )
            auth.signOut()

            true

        } catch (e: Exception) {

            // Cierra cualquier sesión iniciada durante el registro.
            auth.signOut()

            // Intenta eliminar únicamente la cuenta creada en este registro.
            try {
                cuentaCreada?.delete()?.awaitResult()
            } catch (_: Exception) {
                // Si falla, será necesario revisar la cuenta posteriormente.
            }

            false
        }
    }

    // ---- Inicio de sesión con Firebase Authentication ----

    suspend fun login(
        email: String,
        password: String
    ): Usuario? {

        return try {
            val correo = email.trim().lowercase()

            val resultado = auth.signInWithEmailAndPassword(
                correo,
                password
            ).awaitResult()

            val firebaseUser = resultado.user
                ?: return null

            val usuarioLocal = dao.buscarUsuario(firebaseUser.uid)

            if (usuarioLocal != null) {

                usuarioLocal

            } else {

                // Perfil provisional hasta conectar Spring Boot.
                val usuario = Usuario(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: correo,
                    nombre = firebaseUser.displayName
                        ?: correo.substringBefore("@"),
                    rol = "TRABAJADOR_RELATOR",
                    activo = true
                )

                dao.guardarUsuario(usuario)

                usuario
            }

        } catch (e: Exception) {
            null
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    // ---- Recuperación de sesión ----

    suspend fun restaurarSesion(): Usuario? {
        val firebaseUser = auth.currentUser ?: return null

        return try {
            val usuarioLocal = dao.buscarUsuario(firebaseUser.uid)

            if (usuarioLocal != null) {
                usuarioLocal
            } else {
                // Perfil provisional hasta conectar Spring Boot.
                val usuario = Usuario(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    nombre = firebaseUser.displayName
                        ?: firebaseUser.email?.substringBefore("@")
                        ?: "Usuario",
                    rol = "TRABAJADOR_RELATOR",
                    activo = true
                )

                dao.guardarUsuario(usuario)
                usuario
            }
        } catch (e: Exception) {
            null
        }
    }

    // ---- Coordinador inicial ----

    // Ya no se crea un coordinador ficticio en Room.
    suspend fun sembrarCoordinador() {
        // La inicialización del coordinador corresponderá al backend.
    }

    // ---- Dataset ficticio de salas y reservas ----

    suspend fun sembrar(hoy: String) {

        if (dao.contarSalas() > 0) return

        dao.guardarSalas(
            listOf(
                Sala(1, "Sala A101", 30, true, false, true),
                Sala(2, "Lab Computación 1", 20, true, true, true),
                Sala(3, "Sala B201", 40, false, false, false),
                Sala(4, "Multiuso Auditorio", 80, true, false, true)
            )
        )

        dao.guardarReservas(
            listOf(
                Reserva(
                    salaId = 1,
                    fecha = hoy,
                    bloque = 0,
                    relator = "Ana Pérez",
                    actividad = "Excel Avanzado",
                    grupoId = "demo_excel"
                ),
                Reserva(
                    salaId = 2,
                    fecha = hoy,
                    bloque = 1,
                    relator = "Luis Soto",
                    actividad = "Python Básico",
                    grupoId = "demo_python"
                )
            )
        )
    }
}
