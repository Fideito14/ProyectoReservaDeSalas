package cl.DSY1105.reservasalas.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

// Usuarios con contraseña protegida (hash + sal). El rol sale de la BD.
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val email: String,
    val nombre: String,
    val salt: String,
    val hash: String,
    val esCoordinador: Boolean
)