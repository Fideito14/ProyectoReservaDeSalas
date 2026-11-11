
package cl.DSY1105.reservasalas.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reservas",
    indices = [
        Index(
            value = ["salaId", "fecha", "bloque"],
            unique = true
        ),
        Index(value = ["grupoId"])
    ]
)
data class Reserva(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val salaId: Long,
    val fecha: String,
    val bloque: Int,
    val relator: String,
    val actividad: String,
    @ColumnInfo(defaultValue = "''")
    val grupoId: String
)
