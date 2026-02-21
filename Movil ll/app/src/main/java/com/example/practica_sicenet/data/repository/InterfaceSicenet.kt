package com.example.practica_sicenet.data.repository

import com.example.practica_sicenet.data.Alumno
import com.example.practica_sicenet.data.CalificacionFinal
import com.example.practica_sicenet.data.CalificacionUnidad
import com.example.practica_sicenet.data.CargaAcademica
import com.example.practica_sicenet.data.Kardex

interface InterfaceSicenet {
    suspend fun establishSession()
    suspend fun accesoLogin(matricula: String, contrasenia: String): Result<String>
    suspend fun getProfile(): Result<Alumno>
    suspend fun getCargaAcademica(): Result<List<CargaAcademica>>
    suspend fun getKardex(lineamiento: Int): Result<List<Kardex>>
    suspend fun getCalifUnidades(): Result<List<CalificacionUnidad>>
    suspend fun getCalifFinales(modEducativo: Int): Result<List<CalificacionFinal>>
}
