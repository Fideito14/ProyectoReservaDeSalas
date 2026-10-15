package cl.DSY1105.reservasalas.data.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Índice único (sala, fecha, bloque): la BD misma impide el tope de horarios.
@Entity(tableName = "reservas", indices = [Index(value = ["salaId", "fecha", "bloque"], unique = true)])
data class Reserva(@PrimaryKey(autoGenerate = true) val id: Long = 0, val salaId: Long,
                   val fecha: String, val bloque: Int, val relator: String, val actividad: String)