package com.example.practica_sicenet.data.repository

import android.util.Log
import com.example.practica_sicenet.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class SicenetRepository : InterfaceSicenet {

    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) { cookieStore[url.host] = cookies }
        override fun loadForRequest(url: HttpUrl): List<Cookie> = cookieStore[url.host] ?: listOf()
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://sicenet.surguanajuato.tecnm.mx/")
        .client(client)
        .build()

    private val apiService = retrofit.create(SicenetApiService::class.java)

    override suspend fun establishSession() {
        try { apiService.establishSession() } catch (e: Exception) {}
    }

    private fun escapeXml(text: String): String = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

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
        return raw?.let { unescapeJson(it) }
    }

    override suspend fun accesoLogin(matricula: String, contrasenia: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            establishSession()
            val soap = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><accesoLogin xmlns="http://tempuri.org/"><strMatricula>${escapeXml(matricula)}</strMatricula><strContrasenia>${escapeXml(contrasenia)}</strContrasenia><tipoUsuario>ALUMNO</tipoUsuario></accesoLogin></soap:Body></soap:Envelope>"""
            val body = soap.toRequestBody("text/xml".toMediaType())
            val response = apiService.accesoLogin("\"http://tempuri.org/accesoLogin\"", body)
            val result = extractTagContent(response.body()?.string() ?: "", "accesoLoginResult")
            if (result != null) Result.success(result) else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getProfile(): Result<Alumno> = withContext(Dispatchers.IO) {
        try {
            val soap = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" /></soap:Body></soap:Envelope>"""
            val response = apiService.getAlumnoAcademicoWithLineamiento("\"http://tempuri.org/getAlumnoAcademicoWithLineamiento\"", soap.toRequestBody("text/xml".toMediaType()))
            val json = extractTagContent(response.body()?.string() ?: "", "getAlumnoAcademicoWithLineamientoResult")
            if (json != null) Result.success(Alumno.fromJson(json)) else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getCargaAcademica(): Result<List<CargaAcademica>> = withContext(Dispatchers.IO) {
        try {
            val soap = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><getCargaAcademicaByAlumno xmlns="http://tempuri.org/" /></soap:Body></soap:Envelope>"""
            val response = apiService.getCargaAcademicaByAlumno("\"http://tempuri.org/getCargaAcademicaByAlumno\"", soap.toRequestBody("text/xml".toMediaType()))
            val json = extractTagContent(response.body()?.string() ?: "", "getCargaAcademicaByAlumnoResult")
            Result.success(CargaAcademica.fromJsonList(json ?: ""))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getKardex(lineamiento: Int): Result<List<Kardex>> = withContext(Dispatchers.IO) {
        try {
            val soap = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><getAllKardexConPromedioByAlumno xmlns="http://tempuri.org/"><aluLineamiento>$lineamiento</aluLineamiento></getAllKardexConPromedioByAlumno></soap:Body></soap:Envelope>"""
            val response = apiService.getAllKardexConPromedioByAlumno("\"http://tempuri.org/getAllKardexConPromedioByAlumno\"", soap.toRequestBody("text/xml".toMediaType()))
            val json = extractTagContent(response.body()?.string() ?: "", "getAllKardexConPromedioByAlumnoResult")
            Result.success(Kardex.fromJsonList(json ?: ""))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getCalifUnidades(): Result<List<CalificacionUnidad>> = withContext(Dispatchers.IO) {
        try {
            val soap = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><getCalifUnidadesByAlumno xmlns="http://tempuri.org/" /></soap:Body></soap:Envelope>"""
            val response = apiService.getCalifUnidadesByAlumno("\"http://tempuri.org/getCalifUnidadesByAlumno\"", soap.toRequestBody("text/xml".toMediaType()))
            val json = extractTagContent(response.body()?.string() ?: "", "getCalifUnidadesByAlumnoResult")
            Result.success(CalificacionUnidad.fromJsonList(json ?: ""))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getCalifFinales(modEducativo: Int): Result<List<CalificacionFinal>> = withContext(Dispatchers.IO) {
        try {
            val soap = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><getAllCalifFinalByAlumnos xmlns="http://tempuri.org/"><bytModEducativo>$modEducativo</bytModEducativo></getAllCalifFinalByAlumnos></soap:Body></soap:Envelope>"""
            val response = apiService.getAllCalifFinalByAlumnos("\"http://tempuri.org/getAllCalifFinalByAlumnos\"", soap.toRequestBody("text/xml".toMediaType()))
            val json = extractTagContent(response.body()?.string() ?: "", "getAllCalifFinalByAlumnosResult")
            Result.success(CalificacionFinal.fromJsonList(json ?: ""))
        } catch (e: Exception) { Result.failure(e) }
    }
}
