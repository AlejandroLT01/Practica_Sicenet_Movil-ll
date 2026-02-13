package com.example.practica_sicenet.data

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface SicenetApiService {
    @Headers("Content-Type: text/xml; charset=utf-8")
    @POST("ws/wsalumnos.asmx")
    suspend fun accesoLogin(
        @Header("SOAPAction") soapAction: String,
        @Body requestBody: RequestBody
    ): Response<ResponseBody>

    @Headers("Content-Type: text/xml; charset=utf-8")
    @POST("ws/wsalumnos.asmx")
    suspend fun getAlumnoAcademicoWithLineamiento(
        @Header("SOAPAction") soapAction: String,
        @Body requestBody: RequestBody
    ): Response<ResponseBody>
}
