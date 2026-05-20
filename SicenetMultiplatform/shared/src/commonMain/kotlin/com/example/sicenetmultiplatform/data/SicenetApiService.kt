package com.example.sicenetmultiplatform.data

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class SicenetApiService(private val client: HttpClient) {

    private val baseUrl = "https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx"

    suspend fun establishSession(): HttpResponse {
        return client.get(baseUrl)
    }

    private fun escapeXml(text: String): String = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    private suspend fun soapRequest(action: String, body: String): String {
        val response = client.post(baseUrl) {
            header("SOAPAction", "\"http://tempuri.org/$action\"")
            header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            contentType(ContentType.Text.Xml)
            setBody("<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>$body</soap:Body></soap:Envelope>")
        }
        return response.bodyAsText()
    }

    suspend fun accesoLogin(matricula: String, contrasenia: String): String {
        val body = "<accesoLogin xmlns=\"http://tempuri.org/\"><strMatricula>${escapeXml(matricula)}</strMatricula><strContrasenia>${escapeXml(contrasenia)}</strContrasenia><tipoUsuario>ALUMNO</tipoUsuario></accesoLogin>"
        return soapRequest("accesoLogin", body)
    }

    suspend fun getAlumnoAcademicoWithLineamiento(): String {
        return soapRequest("getAlumnoAcademicoWithLineamiento", "<getAlumnoAcademicoWithLineamiento xmlns=\"http://tempuri.org/\" />")
    }

    suspend fun getCargaAcademicaByAlumno(): String {
        return soapRequest("getCargaAcademicaByAlumno", "<getCargaAcademicaByAlumno xmlns=\"http://tempuri.org/\" />")
    }

    suspend fun getAllKardexConPromedioByAlumno(lineamiento: Int): String {
        val body = """
            <getAllKardexConPromedioByAlumno xmlns="http://tempuri.org/">
                <aluLineamiento>$lineamiento</aluLineamiento>
            </getAllKardexConPromedioByAlumno>
        """.trimIndent()
        return soapRequest("getAllKardexConPromedioByAlumno", body)
    }

    suspend fun getCalifUnidadesByAlumno(): String {
        return soapRequest("getCalifUnidadesByAlumno", "<getCalifUnidadesByAlumno xmlns=\"http://tempuri.org/\" />")
    }

    suspend fun getAllCalifFinalByAlumnos(modEducativo: Int): String {
        val body = """
            <getAllCalifFinalByAlumnos xmlns="http://tempuri.org/">
                <bytModEducativo>$modEducativo</bytModEducativo>
            </getAllCalifFinalByAlumnos>
        """.trimIndent()
        return soapRequest("getAllCalifFinalByAlumnos", body)
    }
}
