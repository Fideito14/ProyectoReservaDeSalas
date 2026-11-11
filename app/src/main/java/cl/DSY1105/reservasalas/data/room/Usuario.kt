package cl.DSY1105.reservasalas.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey
    val uid: String,
    val email: String,
    val nombre: String,
    val rol: String,
    val activo: Boolean
) {
    // Compatibilidad temporal con la pantalla de usuarios.
    val esCoordinador: Boolean
        get() = rol == "COORDINADOR_INFRAESTRUCTURA"
}
