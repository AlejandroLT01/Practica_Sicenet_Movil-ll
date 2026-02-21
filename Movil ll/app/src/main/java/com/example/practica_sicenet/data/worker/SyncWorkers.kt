package com.example.practica_sicenet.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.practica_sicenet.data.local.SicenetDatabase
import com.example.practica_sicenet.data.repository.SicenetRepository
import com.example.practica_sicenet.data.Alumno
import com.example.practica_sicenet.data.CargaAcademica
import com.example.practica_sicenet.data.Kardex
import com.example.practica_sicenet.data.CalificacionUnidad
import com.example.practica_sicenet.data.CalificacionFinal

class FetchProfileWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = SicenetRepository()
        val matricula = inputData.getString("matricula") ?: ""
        val password = inputData.getString("password") ?: ""

        if (repository.accesoLogin(matricula, password).isSuccess) {
            val profileResult = repository.getProfile()
            if (profileResult.isSuccess) {
                val db = SicenetDatabase.getDatabase(applicationContext)
                profileResult.getOrNull()?.let { 
                    db.sicenetDao().insertAlumno(it.copy(matricula = matricula)) 
                }
                return Result.success()
            }
        }
        return Result.failure()
    }
}

class FetchCargaWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = SicenetRepository()
        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val matricula = sharedPref.getString("matricula", "") ?: ""
        val password = sharedPref.getString("password", "") ?: ""

        // Re-autenticar para asegurar cookie de sesión
        if (repository.accesoLogin(matricula, password).isSuccess) {
            val result = repository.getCargaAcademica()
            if (result.isSuccess) {
                val db = SicenetDatabase.getDatabase(applicationContext)
                db.sicenetDao().clearCarga()
                db.sicenetDao().insertCarga(result.getOrNull() ?: emptyList())
                return Result.success()
            }
        }
        return Result.failure()
    }
}

class FetchKardexWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = SicenetRepository()
        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val matricula = sharedPref.getString("matricula", "") ?: ""
        val password = sharedPref.getString("password", "") ?: ""
        val lineamiento = inputData.getInt("lineamiento", 1)

        if (repository.accesoLogin(matricula, password).isSuccess) {
            val result = repository.getKardex(lineamiento)
            if (result.isSuccess) {
                val db = SicenetDatabase.getDatabase(applicationContext)
                db.sicenetDao().clearKardex()
                db.sicenetDao().insertKardex(result.getOrNull() ?: emptyList())
                return Result.success()
            }
        }
        return Result.failure()
    }
}

class FetchCalifUnidadesWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = SicenetRepository()
        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val matricula = sharedPref.getString("matricula", "") ?: ""
        val password = sharedPref.getString("password", "") ?: ""

        if (repository.accesoLogin(matricula, password).isSuccess) {
            val result = repository.getCalifUnidades()
            if (result.isSuccess) {
                val db = SicenetDatabase.getDatabase(applicationContext)
                db.sicenetDao().clearCalifUnidades()
                db.sicenetDao().insertCalifUnidades(result.getOrNull() ?: emptyList())
                return Result.success()
            }
        }
        return Result.failure()
    }
}

class FetchCalifFinalesWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = SicenetRepository()
        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val matricula = sharedPref.getString("matricula", "") ?: ""
        val password = sharedPref.getString("password", "") ?: ""
        val mod = inputData.getInt("mod", 1)

        if (repository.accesoLogin(matricula, password).isSuccess) {
            val result = repository.getCalifFinales(mod)
            if (result.isSuccess) {
                val db = SicenetDatabase.getDatabase(applicationContext)
                db.sicenetDao().clearCalifFinales()
                db.sicenetDao().insertCalifFinales(result.getOrNull() ?: emptyList())
                return Result.success()
            }
        }
        return Result.failure()
    }
}
