
package cl.DSY1105.reservasalas.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ---- Salas ----

    @Query("SELECT * FROM salas ORDER BY nombre")
    fun salas(): Flow<List<Sala>>

    @Query("SELECT COUNT(*) FROM salas")
    suspend fun contarSalas(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarSalas(s: List<Sala>)


    // ---- Reservas ----

    @Query("SELECT * FROM reservas WHERE fecha = :f")
    fun reservas(f: String): Flow<List<Reserva>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarReservas(r: List<Reserva>)

    // Inserta todos los bloques de una reserva en una transacción.
    // Si uno falla por estar ocupado, se revierte el grupo completo.
    @Transaction
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarGrupo(reservas: List<Reserva>)

    // Cancela solamente el bloque seleccionado.
    @Query("DELETE FROM reservas WHERE id = :reservaId")
    suspend fun eliminarReserva(reservaId: Long)


    // ---- Usuarios ----

    @Query("SELECT * FROM usuarios WHERE uid = :uid")
    suspend fun buscarUsuario(uid: String): Usuario?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios ORDER BY nombre")
    fun usuarios(): Flow<List<Usuario>>
}
