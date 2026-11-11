
package cl.DSY1105.reservasalas.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Sala::class, Reserva::class, Usuario::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dao(): AppDao

    companion object {

        @Volatile
        private var I: AppDatabase? = null

        // Migración de versión 2 a 3.
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

        // Migración de versión 3 a 4.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    ALTER TABLE reservas
                    ADD COLUMN grupoId TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    UPDATE reservas
                    SET grupoId = 'legacy_' || id
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_reservas_grupoId
                    ON reservas(grupoId)
                    """.trimIndent()
                )
            }
        }

        // Migración de versión 4 a 5.
        // Garantiza que no existan reservas duplicadas
        // para una misma sala, fecha y bloque.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_reservas_salaId_fecha_bloque
                    ON reservas(salaId, fecha, bloque)
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
                    .addMigrations(
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .build()
                    .also {
                        I = it
                    }
            }
    }
}
