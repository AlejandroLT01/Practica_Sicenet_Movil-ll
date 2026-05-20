package com.example.sicenetmultiplatform.data.repository

import com.example.sicenetmultiplatform.data.*

interface InterfaceSicenet {
    suspend fun establishSession()
    suspend fun accesoLogin(matricula: String, contrasenia: String): Result<String>
    suspend fun getProfile(): Result<Alumno>
    suspend fun getCargaAcademica(): Result<List<CargaAcademica>>
    suspend fun getKardex(lineamiento: Int): Result<List<Kardex>>
    suspend fun getCalifUnidades(): Result<List<CalificacionUnidad>>
    suspend fun getCalifFinales(modEducativo: Int): Result<List<CalificacionFinal>>
}
