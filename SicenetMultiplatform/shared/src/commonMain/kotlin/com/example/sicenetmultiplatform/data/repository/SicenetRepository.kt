package com.example.sicenetmultiplatform.data.repository

import com.example.sicenetmultiplatform.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SicenetRepository(private val apiService: SicenetApiService) : InterfaceSicenet {

    override suspend fun establishSession() {
        try { apiService.establishSession() } catch (e: Exception) {
            println("Sicenet: Session error: ${e.message}")
        }
    }

    private fun unescapeJson(text: String): String {
        return text.replace("&quot;", "\"")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("<![CDATA[", "")
            .replace("]]>", "")
            .trim()
    }

    private fun extractTagContent(xml: String, tagName: String): String? {
        val pattern = "<(?:\\w+:)?$tagName(?:\\s+[^>]*)?>(.*?)</(?:\\w+:)?$tagName>".toRegex(RegexOption.DOT_MATCHES_ALL)
        val raw = pattern.find(xml)?.groupValues?.get(1)
        println("Sicenet: Extracted tag [$tagName]: ${raw?.take(100)}...")
        return raw?.let { unescapeJson(it) }
    }

    override suspend fun accesoLogin(matricula: String, contrasenia: String): Result<String> {
        return try {
            establishSession()
            val response = apiService.accesoLogin(matricula, contrasenia)
            val result = extractTagContent(response, "accesoLoginResult")
            println("Sicenet: Login result extracted: $result")
            
            if (result != null && result.contains("\"acceso\":true", ignoreCase = true)) {
                 Result.success(result) 
            } else if (result != null && result.contains("contraseña", ignoreCase = true)) {
                 Result.failure(Exception("Contraseña incorrecta"))
            } else {
                 Result.failure(Exception("Error en credenciales o servidor"))
            }
        } catch (e: Exception) { 
            println("Sicenet: Login exception: ${e.message}")
            Result.failure(e) 
        }
    }

    override suspend fun getProfile(): Result<Alumno> {
        return try {
            val response = apiService.getAlumnoAcademicoWithLineamiento()
            val json = extractTagContent(response, "getAlumnoAcademicoWithLineamientoResult")
            if (json != null) Result.success(Alumno.fromJson(json)) else Result.failure(Exception("Error al obtener perfil"))
        } catch (e: Exception) { 
            Result.failure(e) 
        }
    }

    override suspend fun getCargaAcademica(): Result<List<CargaAcademica>> {
        return try {
            val response = apiService.getCargaAcademicaByAlumno()
            val json = extractTagContent(response, "getCargaAcademicaByAlumnoResult")
            if (json != null) Result.success(CargaAcademica.fromJsonList(json)) else Result.success(emptyList())
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getKardex(lineamiento: Int): Result<List<Kardex>> {
        return try {
            val response = apiService.getAllKardexConPromedioByAlumno(lineamiento)
            val json = extractTagContent(response, "getAllKardexConPromedioByAlumnoResult")
            if (json != null) Result.success(Kardex.fromJsonList(json)) else Result.success(emptyList())
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getCalifUnidades(): Result<List<CalificacionUnidad>> {
        return try {
            val response = apiService.getCalifUnidadesByAlumno()
            val json = extractTagContent(response, "getCalifUnidadesByAlumnoResult")
            if (json != null) Result.success(CalificacionUnidad.fromJsonList(json)) else Result.success(emptyList())
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getCalifFinales(modEducativo: Int): Result<List<CalificacionFinal>> {
        return try {
            val response = apiService.getAllCalifFinalByAlumnos(modEducativo)
            val json = extractTagContent(response, "getAllCalifFinalByAlumnosResult")
            if (json != null) Result.success(CalificacionFinal.fromJsonList(json)) else Result.success(emptyList())
        } catch (e: Exception) { Result.failure(e) }
    }
}
