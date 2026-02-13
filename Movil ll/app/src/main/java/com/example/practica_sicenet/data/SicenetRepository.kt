package com.example.practica_sicenet.data

import android.util.Log
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

class SicenetRepository {

    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            Log.d("SicenetRepo", "Guardando cookies: $cookies")
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val cookies = cookieStore[url.host] ?: listOf()
            Log.d("SicenetRepo", "Cargando cookies para ${url.host}: $cookies")
            return cookies
        }
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://sicenet.surguanajuato.tecnm.mx/")
        .client(client)
        .build()

    private val apiService = retrofit.create(SicenetApiService::class.java)

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

    suspend fun accesoLogin(matricula: String, contrasenia: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val soapRequest = """
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <accesoLogin xmlns="http://tempuri.org/">
      <strMatricula>${escapeXml(matricula)}</strMatricula>
      <strContrasenia>${escapeXml(contrasenia)}</strContrasenia>
      <tipoUsuario>ALUMNO</tipoUsuario>
    </accesoLogin>
  </soap:Body>
</soap:Envelope>
            """.trim()

            val body = soapRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())

            val response = apiService.accesoLogin("\"http://tempuri.org/accesoLogin\"", body)

            if (response.isSuccessful) {
                val responseBody = response.body()?.string() ?: ""
                val result = extractTagContent(responseBody, "accesoLoginResult")
                if (result != null) {
                    Result.success(result)
                } else {
                    Result.failure(Exception("Error en respuesta del servidor. Verifique credenciales."))
                }
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("SicenetRepo", "Excepción", e)
            Result.failure(e)
        }
    }

    suspend fun getAlumnoAcademicoWithLineamiento(): Result<Alumno> = withContext(Dispatchers.IO) {
        try {
            val soapRequest = """
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" />
  </soap:Body>
</soap:Envelope>
            """.trim()

            val body = soapRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())

            val response = apiService.getAlumnoAcademicoWithLineamiento("\"http://tempuri.org/getAlumnoAcademicoWithLineamiento\"", body)

            if (response.isSuccessful) {
                val responseBody = response.body()?.string() ?: ""
                val jsonString = extractTagContent(responseBody, "getAlumnoAcademicoWithLineamientoResult")

                if (jsonString != null) {
                    val alumno = Alumno.fromJson(jsonString)
                    Result.success(alumno)
                } else {
                    Result.failure(Exception("No se pudo recuperar el perfil."))
                }
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
