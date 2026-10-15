package cl.DSY1105.reservasalas.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Sala::class, Reserva::class, Usuario::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao
    companion object {
        @Volatile private var I: AppDatabase? = null
        fun get(c: Context): AppDatabase = I ?: synchronized(this) {
            Room.databaseBuilder(c.applicationContext, AppDatabase::class.java, "reservas_db").build().also { I = it }
        }
    }
}