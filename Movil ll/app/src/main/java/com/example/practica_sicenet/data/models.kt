package com.example.practica_sicenet.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Alumno(
    @SerialName("nombre") val nombre: String = "",
    @SerialName("matricula") val matricula: String = "",
    @SerialName("carrera") val carrera: String = "",
    @SerialName("semestreActual") val semestre: Int = 0,
    @SerialName("especialidad") val especialidad: String = "",
    @SerialName("estatus") val estatus: String = "",
    @SerialName("creditosTotales") val creditosTotales: Int = 0,
    @SerialName("creditosActuales") val creditosActuales: Int = 0,
    @SerialName("inscrito") val inscrito: Boolean = false,
    @SerialName("modEducativo") val modEducativo: Int = 0,
    @SerialName("adeudo") val adeudo: Boolean = false,
    @SerialName("fechaReinscripcion") val fechaReinscripcion: String = ""
)
