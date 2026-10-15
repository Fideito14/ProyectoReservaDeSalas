package cl.DSY1105.reservasalas.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM salas ORDER BY nombre") fun salas(): Flow<List<Sala>>
    @Query("SELECT * FROM reservas WHERE fecha = :f") fun reservas(f: String): Flow<List<Reserva>>
    @Query("SELECT COUNT(*) FROM salas") suspend fun contarSalas(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun guardarSalas(s: List<Sala>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun guardarReservas(r: List<Reserva>)
    @Insert suspend fun insertar(r: Reserva): Long // lanza SQLiteConstraintException si hay tope
    @Delete suspend fun eliminar(r: Reserva)
    @Query("SELECT * FROM usuarios WHERE email = :email") suspend fun buscarUsuario(email: String): Usuario?
    @Insert suspend fun insertarUsuario(u: Usuario) // falla si el correo ya existe
    @Query("SELECT * FROM usuarios ORDER BY nombre") fun usuarios(): Flow<List<Usuario>>
    @Query("UPDATE usuarios SET esCoordinador = :c WHERE email = :email") suspend fun cambiarRol(email: String, c: Boolean)
}