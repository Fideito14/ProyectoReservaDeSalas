package cl.DSY1105.reservasalas.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salas")
data class Sala(@PrimaryKey val id: Long, val nombre: String, val capacidad: Int,
                val proyector: Boolean, val computadores: Boolean, val climatizacion: Boolean)