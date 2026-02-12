package com.example.practica_sicenet.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Repository interface following the pattern in Google Codelabs.
 */
interface SicenetRepository {
    suspend fun accesoLogin(matricula: String, contrasenia: String, tipoUsuario: String): Result<String>
    suspend fun getAlumnoAcademicoWithLineamiento(): Result<String>
}

/**
 * Network implementation of the repository.
 */
class NetworkSicenetRepository : SicenetRepository {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            private val cookieStore = mutableMapOf<String, List<Cookie>>()

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                Log.d("SicenetRepo", "Saving cookies for ${url.host}: $cookies")
                cookieStore[url.host] = cookies
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val cookies = cookieStore[url.host] ?: listOf()
                Log.d("SicenetRepo", "Loading cookies for ${url.host}: $cookies")
                return cookies
            }
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val baseUrl = "https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx"

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun extractTagContent(xml: String, tagName: String): String? {
        val pattern = "<(?:\\w+:)?$tagName(?:\\s+[^>]*)?>(.*?)</(?:\\w+:)?$tagName>".toRegex(RegexOption.DOT_MATCHES_ALL)
        return pattern.find(xml)?.groupValues?.get(1)
    }

    override suspend fun accesoLogin(matricula: String, contrasenia: String, tipoUsuario: String): Result<String> = withContext(Dispatchers.IO) {
        val soapRequest = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <accesoLogin xmlns="http://tempuri.org/">
                  <strMatricula>${escapeXml(matricula)}</strMatricula>
                  <strContrasenia>${escapeXml(contrasenia)}</strContrasenia>
                  <tipoUsuario>${escapeXml(tipoUsuario)}</tipoUsuario>
                </accesoLogin>
              </soap:Body>
            </soap:Envelope>
        """.trim()

        val body = soapRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(baseUrl)
            .post(body)
            .addHeader("SOAPAction", "\"http://tempuri.org/accesoLogin\"")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("SicenetRepo", "Login Response: $responseBody")
                
                if (!response.isSuccessful) return@withContext Result.failure(Exception("Error HTTP: ${response.code}"))
                
                val result = extractTagContent(responseBody, "accesoLoginResult")
                if (result != null) {
                    Result.success(result)
                } else {
                    Result.failure(Exception("Respuesta inválida del servidor"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAlumnoAcademicoWithLineamiento(): Result<String> = withContext(Dispatchers.IO) {
        val soapRequest = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" />
              </soap:Body>
            </soap:Envelope>
        """.trim()

        val body = soapRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(baseUrl)
            .post(body)
            .addHeader("SOAPAction", "\"http://tempuri.org/getAlumnoAcademicoWithLineamiento\"")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("SicenetRepo", "Profile Response: $responseBody")
                
                if (!response.isSuccessful) return@withContext Result.failure(Exception("Error HTTP: ${response.code}"))
                
                val result = extractTagContent(responseBody, "getAlumnoAcademicoWithLineamientoResult")
                if (result != null) {
                    Result.success(result)
                } else {
                    Result.failure(Exception("No se encontró el perfil en la respuesta."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
