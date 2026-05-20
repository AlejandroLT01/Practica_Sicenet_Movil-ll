package com.example.sicenetmultiplatform.data

import com.example.sicenetmultiplatform.data.repository.LocalRepository
import com.example.sicenetmultiplatform.data.repository.SicenetRepository

class SicenetSyncEngine(
    private val localRepository: LocalRepository,
    private val sicenetRepository: SicenetRepository
) {
    suspend fun syncProfile(matricula: String, contrasenia: String): Result<Alumno> {
        val loginResult = sicenetRepository.accesoLogin(matricula, contrasenia)
        if (loginResult.isFailure) return Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
        
        val profileResult = sicenetRepository.getProfile()
        profileResult.getOrNull()?.let {
            localRepository.insertAlumno(it.copy(matricula = matricula))
        }
        return profileResult
    }

    suspend fun syncCarga(matricula: String, contrasenia: String): Result<List<CargaAcademica>> {
        val loginResult = sicenetRepository.accesoLogin(matricula, contrasenia)
        if (loginResult.isFailure) return Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))

        val result = sicenetRepository.getCargaAcademica()
        result.getOrNull()?.let { localRepository.saveCarga(it) }
        return result
    }

    suspend fun syncKardex(matricula: String, contrasenia: String, lineamiento: Int): Result<List<Kardex>> {
        val loginResult = sicenetRepository.accesoLogin(matricula, contrasenia)
        if (loginResult.isFailure) return Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))

        val result = sicenetRepository.getKardex(lineamiento)
        result.getOrNull()?.let { localRepository.saveKardex(it) }
        return result
    }

    suspend fun syncUnidades(matricula: String, contrasenia: String): Result<List<CalificacionUnidad>> {
        val loginResult = sicenetRepository.accesoLogin(matricula, contrasenia)
        if (loginResult.isFailure) return Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))

        val result = sicenetRepository.getCalifUnidades()
        result.getOrNull()?.let { localRepository.saveCalifUnidades(it) }
        return result
    }

    suspend fun syncFinales(matricula: String, contrasenia: String, mod: Int): Result<List<CalificacionFinal>> {
        val loginResult = sicenetRepository.accesoLogin(matricula, contrasenia)
        if (loginResult.isFailure) return Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))

        val result = sicenetRepository.getCalifFinales(mod)
        result.getOrNull()?.let { localRepository.saveCalifFinales(it) }
        return result
    }
}
