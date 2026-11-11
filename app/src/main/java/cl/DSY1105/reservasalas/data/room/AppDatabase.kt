package cl.DSY1105.reservasalas.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Sala::class, Reserva::class, Usuario::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dao(): AppDao

    companion object {

        @Volatile
        private var I: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS usuarios")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS usuarios (
                        uid TEXT NOT NULL,
                        email TEXT NOT NULL,
                        nombre TEXT NOT NULL,
                        rol TEXT NOT NULL,
                        activo INTEGER NOT NULL,
                        PRIMARY KEY(uid)
                    )
                    """.trimIndent()
                )
            }
        }

        fun get(c: Context): AppDatabase =
            I ?: synchronized(this) {
                Room.databaseBuilder(
                    c.applicationContext,
                    AppDatabase::class.java,
                    "reservas_db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                    .also { I = it }
            }
    }
}
