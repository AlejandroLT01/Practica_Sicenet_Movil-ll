package com.example.practica_sicenet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "alumno")
data class Alumno(
    @PrimaryKey val matricula: String = "",
    val nombre: String = "",
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
    val lastUpdate: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromJson(jsonString: String): Alumno {
            return try {
                val json = JSONObject(jsonString)
                Alumno(
                    nombre = json.optString("nombre").ifEmpty { json.optString("Nombre") },
                    matricula = json.optString("matricula").ifEmpty { json.optString("Matricula") },
                    carrera = json.optString("carrera").ifEmpty { json.optString("Carrera") },
                    especialidad = json.optString("especialidad").ifEmpty { json.optString("Especialidad") },
                    semestre = json.optInt("semActual", json.optInt("SemActual")),
                    creditosReunidos = json.optInt("cdtosAcumulados", json.optInt("CdtosAcumulados")),
                    creditosActuales = json.optInt("cdtosActuales", json.optInt("CdtosActuales")),
                    estatus = json.optString("estatus").ifEmpty { json.optString("Estatus") },
                    inscrito = json.optBoolean("inscrito", json.optBoolean("Inscrito")),
                    fechaReinscripcion = json.optString("fechaReins").ifEmpty { json.optString("FechaReins") },
                    modEducativo = json.optInt("modEducativo", json.optInt("ModEducativo")),
                    adeudo = json.optBoolean("adeudo", json.optBoolean("Adeudo")),
                )
            } catch (e: Exception) {
                Alumno()
            }
        }
    }
}

@Entity(tableName = "carga_academica")
data class CargaAcademica(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materia: String,
    val docente: String,
    val grupo: String,
    val creditos: Int,
    val lunes: String,
    val martes: String,
    val miercoles: String,
    val jueves: String,
    val viernes: String
) {
    companion object {
        fun fromJsonList(jsonString: String): List<CargaAcademica> {
            val list = mutableListOf<CargaAcademica>()
            try {
                val trimmed = jsonString.trim()
                val arrayStr = when {
                    trimmed.startsWith("{\"d\":") -> JSONObject(trimmed).getString("d")
                    trimmed.startsWith("{") -> {
                        val obj = JSONObject(trimmed)
                        val keys = obj.keys()
                        if (keys.hasNext()) obj.getString(keys.next()) else trimmed
                    }
                    else -> trimmed
                }
                val jsonArray = JSONArray(arrayStr)
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    list.add(CargaAcademica(
                        materia = json.optString("materia").ifEmpty { json.optString("Materia").ifEmpty { json.optString("Asignatura") } },
                        docente = json.optString("docente").ifEmpty { json.optString("Docente").ifEmpty { json.optString("maestro") } },
                        grupo = json.optString("grupo").ifEmpty { json.optString("Grupo") },
                        creditos = json.optInt("creditos", json.optInt("Creditos")),
                        lunes = json.optString("lunes").ifEmpty { json.optString("Lunes") },
                        martes = json.optString("martes").ifEmpty { json.optString("Martes") },
                        miercoles = json.optString("miercoles").ifEmpty { json.optString("Miercoles") },
                        jueves = json.optString("jueves").ifEmpty { json.optString("Jueves") },
                        viernes = json.optString("viernes").ifEmpty { json.optString("Viernes") }
                    ))
                }
            } catch (e: Exception) {}
            return list
        }
    }
}

@Entity(tableName = "kardex")
data class Kardex(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materia: String,
    val calificacion: Int,
    val semestre: Int,
    val creditos: Int,
    val periodo: String
) {
    companion object {
        fun fromJsonList(jsonString: String): List<Kardex> {
            val list = mutableListOf<Kardex>()
            try {
                val trimmed = jsonString.trim()
                val arrayStr = when {
                    trimmed.startsWith("{\"d\":") -> JSONObject(trimmed).getString("d")
                    trimmed.startsWith("{") -> {
                        val obj = JSONObject(trimmed)
                        val keys = obj.keys()
                        var found: String? = null
                        while(keys.hasNext()) {
                            val key = keys.next()
                            if (obj.get(key) is JSONArray) { found = obj.getString(key); break }
                        }
                        found ?: trimmed
                    }
                    else -> trimmed
                }
                
                val jsonArray = JSONArray(arrayStr)
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val materia = json.optString("materia").ifEmpty { json.optString("Materia").ifEmpty { json.optString("Asignatura") } }
                    if (materia.isEmpty() || materia == "null") continue
                    
                    // Procesar calificación (puede venir como "95.0" o "AC")
                    val rawCalif = json.optString("calif").ifEmpty { json.optString("Calif").ifEmpty { json.optString("Promedio").ifEmpty { json.optString("promedio") } } }
                    val califInt = rawCalif.replace(".0", "").toIntOrNull() ?: 0
                    
                    list.add(Kardex(
                        materia = materia,
                        calificacion = califInt,
                        semestre = json.optInt("semestre", json.optInt("Semestre")),
                        creditos = json.optInt("creditos", json.optInt("Creditos")),
                        periodo = json.optString("periodo").ifEmpty { json.optString("Periodo") }
                    ))
                }
            } catch (e: Exception) {}
            return list
        }
    }
}

@Entity(tableName = "calificaciones_unidades")
data class CalificacionUnidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materia: String,
    val unidades: String
) {
    companion object {
        fun fromJsonList(jsonString: String): List<CalificacionUnidad> {
            val list = mutableListOf<CalificacionUnidad>()
            try {
                val array = if (jsonString.trim().startsWith("{\"d\":")) JSONObject(jsonString).getString("d") else jsonString
                val jsonArray = JSONArray(array)
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val materia = json.optString("materia").ifEmpty { json.optString("Materia") }
                    if (materia.isEmpty()) continue
                    
                    val unitsBuilder = StringBuilder()
                    for (u in 1..13) {
                        val cKey = "C$u"; val pKey = "P$u"
                        val valC = json.optString(cKey).ifEmpty { json.optString(cKey.lowercase()) }
                        val valP = json.optString(pKey).ifEmpty { json.optString(pKey.lowercase()) }
                        val finalVal = if (valC.isNotEmpty()) valC else valP
                        if (finalVal.isNotEmpty() && finalVal != "null") {
                            unitsBuilder.append("U$u: $finalVal  ")
                        }
                    }
                    list.add(CalificacionUnidad(materia = materia, unidades = unitsBuilder.toString().trim()))
                }
            } catch (e: Exception) {}
            return list
        }
    }
}

@Entity(tableName = "calificaciones_finales")
data class CalificacionFinal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materia: String,
    val calificacion: Int
) {
    companion object {
        fun fromJsonList(jsonString: String): List<CalificacionFinal> {
            val list = mutableListOf<CalificacionFinal>()
            try {
                val array = if (jsonString.trim().startsWith("{\"d\":")) JSONObject(jsonString).getString("d") else jsonString
                val jsonArray = JSONArray(array)
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val materia = json.optString("materia").ifEmpty { json.optString("Materia") }
                    if (materia.isEmpty()) continue
                    
                    val rawCalif = json.optString("calif").ifEmpty { json.optString("Calif").ifEmpty { json.optString("Promedio") } }
                    val califInt = rawCalif.replace(".0", "").toIntOrNull() ?: 0
                    
                    list.add(CalificacionFinal(materia = materia, calificacion = califInt))
                }
            } catch (e: Exception) {}
            return list
        }
    }
}
