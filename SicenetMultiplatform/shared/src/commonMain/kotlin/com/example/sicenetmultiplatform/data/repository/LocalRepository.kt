package com.example.sicenetmultiplatform.data.repository

import com.example.sicenetmultiplatform.data.*
import com.example.sicenetmultiplatform.data.local.SicenetDao
import kotlinx.coroutines.flow.Flow

class LocalRepository(private val dao: SicenetDao) {
    fun getAlumno(): Flow<Alumno?> = dao.getAlumno()
    fun getCarga(): Flow<List<CargaAcademica>> = dao.getCarga()
    fun getKardex(): Flow<List<Kardex>> = dao.getKardex()
    fun getCalifUnidades(): Flow<List<CalificacionUnidad>> = dao.getCalifUnidades()
    fun getCalifFinales(): Flow<List<CalificacionFinal>> = dao.getCalifFinales()

    suspend fun insertAlumno(alumno: Alumno) {
        // Limpiamos el alumno anterior antes de insertar el nuevo
        // para evitar que se muestre información mezclada o vieja
        dao.clearAlumno()
        dao.insertAlumno(alumno)
    }
    
    suspend fun saveCarga(carga: List<CargaAcademica>) {
        dao.clearCarga()
        dao.insertCarga(carga)
    }

    suspend fun saveKardex(kardex: List<Kardex>) {
        dao.clearKardex()
        dao.insertKardex(kardex)
    }

    suspend fun saveCalifUnidades(calif: List<CalificacionUnidad>) {
        dao.clearCalifUnidades()
        dao.insertCalifUnidades(calif)
    }

    suspend fun saveCalifFinales(calif: List<CalificacionFinal>) {
        dao.clearCalifFinales()
        dao.insertCalifFinales(calif)
    }

    suspend fun clearAllData() {
        dao.clearAlumno()
        dao.clearCarga()
        dao.clearKardex()
        dao.clearCalifUnidades()
        dao.clearCalifFinales()
    }
}
