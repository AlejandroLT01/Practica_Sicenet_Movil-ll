package com.example.practica_sicenet.data.repository

import com.example.practica_sicenet.data.*
import com.example.practica_sicenet.data.local.SicenetDao
import kotlinx.coroutines.flow.Flow

class LocalRepository(private val dao: SicenetDao) {
    fun getAlumno(): Flow<Alumno?> = dao.getAlumno()
    fun getCarga(): Flow<List<CargaAcademica>> = dao.getCarga()
    fun getKardex(): Flow<List<Kardex>> = dao.getKardex()
    fun getCalifUnidades(): Flow<List<CalificacionUnidad>> = dao.getCalifUnidades()
    fun getCalifFinales(): Flow<List<CalificacionFinal>> = dao.getCalifFinales()

    suspend fun insertAlumno(alumno: Alumno) = dao.insertAlumno(alumno)
    
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
}
