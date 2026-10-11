package cl.DSY1105.reservasalas.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

object Bloques { val lista = listOf("08:00 - 09:30", "09:40 - 11:10", "11:20 - 12:50", "14:00 - 15:30", "15:40 - 17:10") }

@Entity(tableName = "salas")
data class Sala(@PrimaryKey val id: Long, val nombre: String, val capacidad: Int,
                val proyector: Boolean, val computadores: Boolean, val climatizacion: Boolean)

// Índice único (sala, fecha, bloque): la BD misma impide el tope de horarios.
@Entity(tableName = "reservas", indices = [Index(value = ["salaId", "fecha", "bloque"], unique = true)])
data class Reserva(@PrimaryKey(autoGenerate = true) val id: Long = 0, val salaId: Long,
                   val fecha: String, val bloque: Int, val relator: String, val actividad: String)

@Dao
interface AppDao {
    @Query("SELECT * FROM salas ORDER BY nombre") fun salas(): Flow<List<Sala>>
    @Query("SELECT * FROM reservas WHERE fecha = :f") fun reservas(f: String): Flow<List<Reserva>>
    @Query("SELECT COUNT(*) FROM salas") suspend fun contarSalas(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun guardarSalas(s: List<Sala>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun guardarReservas(r: List<Reserva>)
    @Insert suspend fun insertar(r: Reserva): Long // lanza SQLiteConstraintException si hay tope
    @Delete suspend fun eliminar(r: Reserva)
}

@Database(entities = [Sala::class, Reserva::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao
    companion object {
        @Volatile private var I: AppDatabase? = null
        fun get(c: Context): AppDatabase = I ?: synchronized(this) {
            Room.databaseBuilder(c.applicationContext, AppDatabase::class.java, "reservas_db").build().also { I = it }
        }
    }
}
