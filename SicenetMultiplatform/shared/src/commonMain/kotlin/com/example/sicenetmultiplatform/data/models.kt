package com.example.sicenetmultiplatform.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.datetime.Clock

// --- MOTOR DE BÚSQUEDA MULTIPLATAFORMA ROBUSTO PARA SICENET ---

fun JsonObject.findString(vararg keywords: String): String {
    // 1. Prioridad: Coincidencia exacta (ignorando mayúsculas)
    for (key in keywords) {
        val entry = this.entries.find { it.key.equals(key, ignoreCase = true) }
        val element = entry?.value
        if (element != null && element is JsonPrimitive) {
            val content = element.content
            if (content.isNotEmpty() && content != "null") return content.trim()
        }
    }
    // 2. Prioridad: Búsqueda parcial (la llave contiene la palabra o viceversa)
    for (word in keywords) {
        if (word.length < 3) continue
        val entry = this.entries.find { 
            it.key.contains(word, ignoreCase = true) || word.contains(it.key, ignoreCase = true) 
        }
        val element = entry?.value
        if (element != null && element is JsonPrimitive) {
            val content = element.content
            if (content.isNotEmpty() && content != "null") return content.trim()
        }
    }
    return ""
}

fun JsonObject.findInt(vararg keywords: String): Int {
    for (key in keywords) {
        val entry = this.entries.find { it.key.equals(key, ignoreCase = true) }
        val element = entry?.value
        if (element != null && element is JsonPrimitive) {
            val raw = element.content.split(".")[0].replace(Regex("[^0-9]"), "")
            if (raw.isNotEmpty()) return raw.toIntOrNull() ?: 0
        }
    }
    for (word in keywords) {
        if (word.length < 3) continue
        val entry = this.entries.find { 
            it.key.contains(word, ignoreCase = true) || word.contains(it.key, ignoreCase = true) 
        }
        val element = entry?.value
        if (element != null && element is JsonPrimitive) {
            val raw = element.content.split(".")[0].replace(Regex("[^0-9]"), "")
            if (raw.isNotEmpty()) return raw.toIntOrNull() ?: 0
        }
    }
    return 0
}

fun findJsonObject(jsonString: String): JsonObject {
    return try {
        val trimmed = jsonString.trim()
        val element = Json.parseToJsonElement(trimmed)
        if (element is JsonObject) {
            val d = element["d"]
            if (d != null) {
                if (d is JsonObject) return d
                if (d is JsonPrimitive && d.isString) return findJsonObject(d.content)
            }
            return element
        }
        if (element is JsonArray && element.isNotEmpty()) return element[0].jsonObject
        JsonObject(emptyMap())
    } catch (e: Exception) {
        JsonObject(emptyMap())
    }
}

fun findJsonArray(jsonString: String): JsonArray {
    return try {
        val trimmed = jsonString.trim()
        val element = Json.parseToJsonElement(trimmed)
        if (element is JsonArray) return element
        if (element is JsonObject) {
            val d = element["d"]
            if (d != null) {
                if (d is JsonArray) return d
                if (d is JsonPrimitive && d.isString) return findJsonArray(d.content)
            }
            val firstArray = element.values.find { it is JsonArray }
            if (firstArray != null) return firstArray as JsonArray
            
            element.values.forEach { 
                if (it is JsonPrimitive && it.isString && it.content.trim().startsWith("[")) {
                    return findJsonArray(it.content)
                }
            }
        }
        JsonArray(emptyList())
    } catch (e: Exception) {
        JsonArray(emptyList())
    }
}

@Serializable
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
    val lastUpdate: Long = 0L
) {
    companion object {
        fun fromJson(jsonString: String): Alumno {
            return try {
                val json = findJsonObject(jsonString)
                Alumno(
                    nombre = json.findString("Nombre", "nombre"),
                    matricula = json.findString("Matricula", "matricula"),
                    carrera = json.findString("Carrera", "carrera"),
                    especialidad = json.findString("Especialidad", "especialidad"),
                    semestre = json.findInt("SemActual", "semestre"),
                    creditosReunidos = json.findInt("CdtosAcumulados", "reunidos"),
                    creditosActuales = json.findInt("CdtosActuales", "actuales"),
                    estatus = json.findString("Estatus", "estatus"),
                    inscrito = json["inscrito"]?.jsonPrimitive?.booleanOrNull ?: json.findString("inscrito").toBoolean(),
                    fechaReinscripcion = json.findString("FechaReins", "fecha"),
                    modEducativo = json.findInt("ModEducativo", "mod").let { if (it == 0) 1 else it },
                    adeudo = json["adeudo"]?.jsonPrimitive?.booleanOrNull ?: json.findString("adeudo").toBoolean(),
                    lastUpdate = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                )
            } catch (e: Exception) { Alumno() }
        }
    }
}

@Serializable
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
            val array = findJsonArray(jsonString)
            return array.mapNotNull { 
                val obj = it.jsonObject
                val materia = obj.findString("Materia", "asignatura")
                if (materia.isEmpty()) null else CargaAcademica(
                    materia = materia,
                    docente = obj.findString("Docente", "maestro"),
                    grupo = obj.findString("Grupo", "grupo"),
                    creditos = obj.findInt("CreditosMateria", "Cdts"),
                    lunes = obj.findString("Lunes"),
                    martes = obj.findString("Martes"),
                    miercoles = obj.findString("Miercoles"),
                    jueves = obj.findString("Jueves"),
                    viernes = obj.findString("Viernes")
                )
            }
        }
    }
}

@Serializable
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
            val array = findJsonArray(jsonString)
            return array.mapNotNull {
                val obj = it.jsonObject
                val materia = obj.findString("Materia", "asignatura")
                if (materia.isEmpty()) null else {
                    val p = obj.findString("P3", "P2", "P1", "Periodo")
                    val a = obj.findString("A3", "A2", "A1")
                    Kardex(
                        materia = materia,
                        calificacion = obj.findInt("Calif", "Promedio"),
                        semestre = obj.findInt("S3", "S2", "S1", "Semestre"),
                        creditos = obj.findInt("Cdts", "Creditos"),
                        periodo = if (a.isNotEmpty()) "$p $a" else p
                    )
                }
            }
        }
    }
}

@Serializable
@Entity(tableName = "calificaciones_unidades")
data class CalificacionUnidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materia: String,
    val unidades: String
) {
    companion object {
        fun fromJsonList(jsonString: String): List<CalificacionUnidad> {
            val array = findJsonArray(jsonString)
            return array.mapNotNull {
                val obj = it.jsonObject
                val materia = obj.findString("Materia")
                if (materia.isEmpty()) null else {
                    val unitsBuilder = StringBuilder()
                    for (u in 1..13) {
                        val valC = obj.findString("C$u")
                        if (valC.isNotEmpty()) unitsBuilder.append("U$u: $valC  ")
                    }
                    CalificacionUnidad(materia = materia, unidades = unitsBuilder.toString().trim())
                }
            }
        }
    }
}

@Serializable
@Entity(tableName = "calificaciones_finales")
data class CalificacionFinal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materia: String,
    val calificacion: Int
) {
    companion object {
        fun fromJsonList(jsonString: String): List<CalificacionFinal> {
            val array = findJsonArray(jsonString)
            return array.mapNotNull {
                val obj = it.jsonObject
                val materia = obj.findString("materia", "Materia")
                if (materia.isEmpty()) null else CalificacionFinal(
                    materia = materia,
                    calificacion = obj.findInt("Calif", "Promedio")
                )
            }
        }
    }
}
