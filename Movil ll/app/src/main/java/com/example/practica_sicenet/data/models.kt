package com.example.practica_sicenet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

// --- MOTOR DE BÚSQUEDA DEFINITIVO PARA SICENET ---

fun JSONObject.findString(vararg keywords: String): String {
    val keysList = mutableListOf<String>()
    this.keys().forEach { keysList.add(it) }

    // 1. Prioridad: Coincidencia exacta o abreviaturas conocidas
    for (key in keywords) {
        val actualKey = keysList.find { it.equals(key, ignoreCase = true) }
        if (actualKey != null) {
            val v = this.optString(actualKey, "")
            if (v.isNotEmpty() && v != "null") return v.trim()
        }
    }

    // 2. Prioridad: Búsqueda parcial (contiene la palabra)
    for (word in keywords) {
        if (word.length < 2) continue
        val fuzzyKey = keysList.find { it.contains(word, ignoreCase = true) || word.contains(it, ignoreCase = true) }
        if (fuzzyKey != null) {
            val v = this.optString(fuzzyKey, "")
            if (v.isNotEmpty() && v != "null") return v.trim()
        }
    }
    return ""
}

fun JSONObject.findInt(vararg keywords: String): Int {
    val keysList = mutableListOf<String>()
    this.keys().forEach { keysList.add(it) }

    for (key in keywords) {
        val actualKey = keysList.find { it.equals(key, ignoreCase = true) }
        if (actualKey != null) {
            val raw = this.optString(actualKey, "0").split(".")[0]
            val clean = raw.replace(Regex("[^0-9]"), "")
            if (clean.isNotEmpty()) return clean.toIntOrNull() ?: 0
        }
    }

    for (word in keywords) {
        if (word.length < 2) continue
        val fuzzyKey = keysList.find { it.contains(word, ignoreCase = true) || word.contains(it, ignoreCase = true) }
        if (fuzzyKey != null) {
            val raw = this.optString(fuzzyKey, "0").split(".")[0]
            val clean = raw.replace(Regex("[^0-9]"), "")
            if (clean.isNotEmpty()) return clean.toIntOrNull() ?: 0
        }
    }
    return 0
}

fun findJsonArray(jsonString: String): JSONArray {
    val trimmed = jsonString.trim()
    if (trimmed.startsWith("[")) return JSONArray(trimmed)
    if (trimmed.startsWith("{")) {
        val obj = JSONObject(trimmed)
        if (obj.has("d")) {
            val d = obj.get("d")
            if (d is JSONArray) return d
            if (d is String) return findJsonArray(d)
        }
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val item = obj.get(key)
            if (item is JSONArray) return item
            if (item is String && (item.startsWith("[") || item.startsWith("{"))) {
                try { return findJsonArray(item) } catch(e: Exception) {}
            }
        }
    }
    return JSONArray()
}

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
                    nombre = json.findString("Nombre", "nombre"),
                    matricula = json.findString("Matricula", "matricula"),
                    carrera = json.findString("Carrera", "carrera"),
                    especialidad = json.findString("Especialidad", "especialidad"),
                    semestre = json.findInt("SemActual", "semestre"),
                    creditosReunidos = json.findInt("CdtosAcumulados", "reunidos"),
                    creditosActuales = json.findInt("CdtosActuales", "actuales"),
                    estatus = json.findString("Estatus", "estatus"),
                    inscrito = json.optBoolean("Inscrito", true),
                    fechaReinscripcion = json.findString("FechaReins", "fecha"),
                    modEducativo = json.findInt("ModEducativo", "mod"),
                    adeudo = json.optBoolean("Adeudo", false),
                )
            } catch (e: Exception) { Alumno() }
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
                val jsonArray = findJsonArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)

                    //Se extrae la materia primero para validar
                    val materiaNom = json.findString("Materia", "materia", "Asignatura")
                    if (materiaNom.isEmpty()) continue

                    list.add(CargaAcademica(
                        materia = materiaNom,
                        docente = json.findString("Docente", "maestro", "Profesor"),
                        grupo = json.findString("Grupo", "grupo"),
                        creditos = json.findInt("CreditosMateria", "Cdts", "creditos", "clvCredito"),
                        lunes = json.findString("Lunes", "lunes"),
                        martes = json.findString("Martes", "martes"),
                        miercoles = json.findString("Miercoles", "miercoles"),
                        jueves = json.findString("Jueves", "jueves"),
                        viernes = json.findString("Viernes", "viernes")
                    ))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                val jsonArray = findJsonArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val materia = json.findString("Materia", "materia", "asignatura", "Asignatura")
                    if (materia.isEmpty()) continue

                    //Se extrae el periodo y año (3ra, luego 2da, luego 1ra oportunidad)
                    val periodoStr = json.findString("P3", "P2", "P1", "Periodo", "Pd")
                    val anioStr = json.findString("A3", "A2", "A1")

                    //Unir periodo y año ("ENE-JUN 2023")
                    val periodoCompleto = if (anioStr.isNotEmpty()) "$periodoStr $anioStr" else periodoStr

                    list.add(Kardex(
                        materia = materia,
                        calificacion = json.findInt("Calif", "Promedio", "calif", "prm"),
                        //S3, luego S2, luego S1
                        semestre = json.findInt("S3", "S2", "S1", "Semestre", "Sem"),
                        creditos = json.findInt("Cdts", "Creditos", "Crd", "Cd", "C"),
                        periodo = periodoCompleto
                    ))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                val jsonArray = findJsonArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val materia = json.findString("Materia", "materia")
                    if (materia.isEmpty()) continue

                    //"UnidadesActivas" dice cuántas unidades mostrar ("111111" = 6 unidades)
                    val activas = json.optString("UnidadesActivas", "")
                    val numUnidades = if (activas.isNotEmpty()) activas.length else 13

                    val unitsBuilder = StringBuilder()
                    for (u in 1..numUnidades) {
                        val valC = json.optString("C$u", "")
                        //Se agrega la unidad si no es null y no está vacía
                        if (valC.isNotEmpty() && valC != "null") {
                            unitsBuilder.append("U$u: $valC  ")
                        }
                    }

                    list.add(CalificacionUnidad(
                        materia = materia,
                        unidades = unitsBuilder.toString().trim().ifEmpty { "Sin calificaciones" }
                    ))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                val jsonArray = findJsonArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val materia = json.findString("materia", "Materia")
                    if (materia.isEmpty()) continue
                    list.add(CalificacionFinal(
                        materia = materia, 
                        calificacion = json.findInt("Calif", "Promedio", "promedio", "calif", "Nota")
                    ))
                }
            } catch (e: Exception) {}
            return list
        }
    }
}
