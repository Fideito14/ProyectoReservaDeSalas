package cl.DSY1105.reservasalas.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM salas ORDER BY nombre")
    fun salas(): Flow<List<Sala>>

    @Query("SELECT * FROM reservas WHERE fecha = :f")
    fun reservas(f: String): Flow<List<Reserva>>

    @Query("SELECT COUNT(*) FROM salas")
    suspend fun contarSalas(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarSalas(s: List<Sala>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarReservas(r: List<Reserva>)

    @Insert
    suspend fun insertar(r: Reserva): Long

    @Delete
    suspend fun eliminar(r: Reserva)

    // Usuarios
    @Query("SELECT * FROM usuarios WHERE uid = :uid")
    suspend fun buscarUsuario(uid: String): Usuario?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios ORDER BY nombre")
    fun usuarios(): Flow<List<Usuario>>

}
