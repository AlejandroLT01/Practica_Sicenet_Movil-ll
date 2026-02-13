package com.example.practica_sicenet.data.repository

import com.example.practica_sicenet.data.Alumno

interface InterfaceSicenet {
    suspend fun establishSession()
    suspend fun accesoLogin(matricula: String, contrasenia: String): Result<String>
    suspend fun getProfile(): Result<Alumno>
}
