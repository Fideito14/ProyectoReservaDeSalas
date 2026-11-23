
package cl.DSY1105.reservasalas.data.repository

import android.util.Log
import cl.DSY1105.reservasalas.data.api.ApiService
import cl.DSY1105.reservasalas.data.dto.LoginRequest
import cl.DSY1105.reservasalas.data.dto.RegistroRequest
import cl.DSY1105.reservasalas.data.dto.toEntity
import cl.DSY1105.reservasalas.data.room.AppDao
import cl.DSY1105.reservasalas.data.room.NetworkMonitor
import cl.DSY1105.reservasalas.data.room.Reserva
import cl.DSY1105.reservasalas.data.room.Sala
import cl.DSY1105.reservasalas.data.room.SessionManager
import cl.DSY1105.reservasalas.data.room.Usuario
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val api: ApiService,
    private val dao: AppDao,
    private val sessionManager: SessionManager,
    private val networkMonitor: NetworkMonitor
) {

    val salas: Flow<List<Sala>> = dao.observarSalas()
    val reservas: Flow<List<Reserva>> = dao.observarReservas()
    val usuarios: Flow<List<Usuario>> = dao.usuarios()

    // ---- Autenticación ----

    suspend fun login(
        correo: String,
        password: String
    ): Result<Usuario> {

        if (!networkMonitor.isOnline()) {
            return Result.failure(
                Exception("Necesitas conexión a internet para iniciar sesión.")
            )
        }

        return try {
            val respuestaLogin = api.login(
                LoginRequest(correo, password)
            )

            Log.d(
                "LOGIN_DEBUG",
                "Respuesta del login: HTTP ${respuestaLogin.code()}"
            )

            if (!respuestaLogin.isSuccessful ||
                respuestaLogin.body() == null
            ) {
                return Result.failure(
                    Exception("Correo o contraseña incorrectos.")
                )
            }

            val token = respuestaLogin.body()!!.token
            sessionManager.saveToken(token)

            Log.d(
                "LOGIN_DEBUG",
                "Token recibido y guardado: ${token.isNotBlank()}"
            )

            val respuestaPerfil = api.getPerfil()

            Log.d(
                "LOGIN_DEBUG",
                "Respuesta del perfil: HTTP ${respuestaPerfil.code()}"
            )

            if (!respuestaPerfil.isSuccessful ||
                respuestaPerfil.body() == null
            ) {
                sessionManager.clearToken()

                return Result.failure(
                    Exception("No se pudo recuperar el perfil del usuario.")
                )
            }

            val usuario = respuestaPerfil.body()!!.toEntity()
            dao.guardarUsuario(usuario)

            Log.d(
                "LOGIN_DEBUG",
                "Login completado y perfil guardado correctamente."
            )

            Result.success(usuario)

        } catch (e: Exception) {
            Log.e(
                "LOGIN_DEBUG",
                "Error durante el login o recuperación del perfil",
                e
            )

            sessionManager.clearToken()
            Result.failure(e)
        }
    }

    suspend fun registrar(
        nombre: String,
        correo: String,
        password: String
    ): Result<String> {

        if (!networkMonitor.isOnline()) {
            return Result.failure(
                Exception("Necesitas conexión a internet para registrarte.")
            )
        }

        return try {
            val respuesta = api.registro(
                RegistroRequest(
                    nombre = nombre,
                    correo = correo,
                    password = password
                )
            )

            if (respuesta.isSuccessful && respuesta.body() != null) {
                Result.success(respuesta.body()!!.mensaje)
            } else {
                Result.failure(
                    Exception("No se pudo completar el registro: ${respuesta.code()}")
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerPerfil(): Result<Usuario> {

        if (!networkMonitor.isOnline()) {
            return Result.failure(
                Exception("Necesitas conexión a internet para recuperar el perfil.")
            )
        }

        return try {
            val respuesta = api.getPerfil()

            if (respuesta.isSuccessful && respuesta.body() != null) {
                val usuario = respuesta.body()!!.toEntity()
                dao.guardarUsuario(usuario)
                Result.success(usuario)
            } else {
                Result.failure(
                    Exception("No se pudo recuperar el perfil: ${respuesta.code()}")
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---- Sincronización de usuarios ----

    suspend fun sincronizarUsuarios(): Result<Unit> {

        if (!networkMonitor.isOnline()) {
            return Result.failure(
                Exception("Sin conexión. Se muestran los usuarios guardados localmente.")
            )
        }

        return try {
            val usuariosRemotos = api.getUsuarios()

            val usuariosLocales = usuariosRemotos.map {
                it.toEntity()
            }

            dao.guardarUsuarios(usuariosLocales)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cerrarSesion() {
        sessionManager.clearToken()
    }

    // ---- Reservas ----
    // Pendiente: conectar cuando el backend de reservas esté implementado.

    suspend fun crearReserva(
        request: cl.DSY1105.reservasalas.data.dto.CrearReservaRequest
    ): Result<Unit> {
        return Result.failure(
            Exception("La creación de reservas todavía no está disponible en el backend.")
        )
    }

    suspend fun cancelarReserva(
        reservaId: Long
    ): Result<Unit> {
        return Result.failure(
            Exception("La cancelación de reservas todavía no está disponible en el backend.")
        )
    }
}
