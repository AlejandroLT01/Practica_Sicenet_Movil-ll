package com.example.sicenetmultiplatform.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.sicenetmultiplatform.data.*
import kotlinx.coroutines.flow.Flow
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Dao
interface SicenetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlumno(alumno: Alumno)

    @Query("DELETE FROM alumno")
    suspend fun clearAlumno()

    @Query("SELECT * FROM alumno LIMIT 1")
    fun getAlumno(): Flow<Alumno?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarga(carga: List<CargaAcademica>)

    @Query("SELECT * FROM carga_academica")
    fun getCarga(): Flow<List<CargaAcademica>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKardex(kardex: List<Kardex>)

    @Query("SELECT * FROM kardex")
    fun getKardex(): Flow<List<Kardex>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalifUnidades(calif: List<CalificacionUnidad>)

    @Query("SELECT * FROM calificaciones_unidades")
    fun getCalifUnidades(): Flow<List<CalificacionUnidad>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalifFinales(calif: List<CalificacionFinal>)

    @Query("SELECT * FROM calificaciones_finales")
    fun getCalifFinales(): Flow<List<CalificacionFinal>>

    @Query("DELETE FROM carga_academica")
    suspend fun clearCarga()

    @Query("DELETE FROM kardex")
    suspend fun clearKardex()

    @Query("DELETE FROM calificaciones_unidades")
    suspend fun clearCalifUnidades()

    @Query("DELETE FROM calificaciones_finales")
    suspend fun clearCalifFinales()
}

@Database(
    entities = [Alumno::class, CargaAcademica::class, Kardex::class, CalificacionUnidad::class, CalificacionFinal::class],
    version = 1,
    exportSchema = false
)
abstract class SicenetDatabase : RoomDatabase() {
    abstract fun sicenetDao(): SicenetDao

    companion object {
        fun getDatabase(builder: RoomDatabase.Builder<SicenetDatabase>): SicenetDatabase {
            return builder
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        }
    }
}
