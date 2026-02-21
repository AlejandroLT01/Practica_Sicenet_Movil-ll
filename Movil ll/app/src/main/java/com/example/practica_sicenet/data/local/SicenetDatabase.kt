package com.example.practica_sicenet.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.practica_sicenet.data.Alumno
import com.example.practica_sicenet.data.CalificacionFinal
import com.example.practica_sicenet.data.CalificacionUnidad
import com.example.practica_sicenet.data.CargaAcademica
import com.example.practica_sicenet.data.Kardex
import kotlinx.coroutines.flow.Flow

@Dao
interface SicenetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlumno(alumno: Alumno)

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

@Database(entities = [Alumno::class, CargaAcademica::class, Kardex::class, CalificacionUnidad::class, CalificacionFinal::class], version = 1, exportSchema = false)
abstract class SicenetDatabase : RoomDatabase() {
    abstract fun sicenetDao(): SicenetDao

    companion object {
        @Volatile
        private var INSTANCE: SicenetDatabase? = null

        fun getDatabase(context: Context): SicenetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SicenetDatabase::class.java,
                    "sicenet_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
