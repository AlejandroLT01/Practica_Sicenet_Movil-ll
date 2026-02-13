package com.example.practica_sicenet.data

import org.json.JSONObject

data class Alumno(
    val nombre: String = "",
    val matricula: String = "",
    val carrera: String = "",
    val especialidad: String = "",
    val semestre: Int = 0,
    val creditosReunidos: Int = 0,
    val creditosActuales: Int = 0,
    val estatus: String = "",
    val inscrito: Boolean = false,
    val fechaReinscripcion: String = "",
    val modEducativo: Int = 0,
    val adeudo: Boolean = false,
) {
    companion object {
        fun fromJson(jsonString: String): Alumno {
            return try {
                val json = JSONObject(jsonString)
                Alumno(
                    nombre = json.optString("nombre"),
                    matricula = json.optString("matricula"),
                    carrera = json.optString("carrera"),
                    especialidad = json.optString("especialidad"),
                    semestre = json.optInt("semActual"),
                    creditosReunidos = json.optInt("cdtosAcumulados"),
                    creditosActuales = json.optInt("cdtosActuales"),
                    estatus = json.optString("estatus"),
                    inscrito = json.optBoolean("inscrito"),
                    fechaReinscripcion = json.optString("fechaReins"),
                    modEducativo = json.optInt("modEducativo"),
                    adeudo = json.optBoolean("adeudo"),
                )
            } catch (e: Exception) {
                Alumno()
            }
        }
    }
}
