package com.example.practica_sicenet.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.practica_sicenet.data.local.SicenetDatabase
import com.example.practica_sicenet.data.repository.SicenetRepository
import com.example.practica_sicenet.data.repository.LocalRepository

// --- 1. PROFILE WORKERS (Inicio de Sesión y Perfil) ---
class FetchProfileWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = SicenetRepository()
        val matricula = inputData.getString("matricula") ?: ""
        val password = inputData.getString("password") ?: ""

        return if (repository.accesoLogin(matricula, password).isSuccess) {
            val profileResult = repository.getProfile()
            if (profileResult.isSuccess) Result.success() else Result.failure()
        } else Result.failure()
    }
}

class StoreProfileWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = SicenetDatabase.getDatabase(applicationContext)
        val dao = db.sicenetDao() // Usamos el DAO directamente para limpiar
        val repository = SicenetRepository()

        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val matricula = sharedPref.getString("matricula", "") ?: ""
        val password = sharedPref.getString("password", "") ?: ""

        return if (repository.accesoLogin(matricula, password).isSuccess) {
            val result = repository.getProfile()
            result.getOrNull()?.let { alumno ->
                // --- CAMBIO CLAVE AQUÍ ---
                dao.clearAlumno() // Borramos al usuario viejo
                dao.insertAlumno(alumno.copy(matricula = matricula)) // Insertamos al nuevo
                // -------------------------
                Result.success()
            } ?: Result.failure()
        } else Result.failure()
    }
}

// --- 2. CARGA WORKERS (Horarios) ---
class FetchCargaWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = SicenetRepository()
        val sp = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sp.getString("matricula", "") ?: ""
        val p = sp.getString("password", "") ?: ""

        return if (repository.accesoLogin(m, p).isSuccess) {
            val res = repository.getCargaAcademica()
            if (res.isSuccess) Result.success() else Result.failure()
        } else Result.failure()
    }
}

class StoreCargaWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = SicenetDatabase.getDatabase(applicationContext)
        val localRepo = LocalRepository(db.sicenetDao())
        val repository = SicenetRepository()
        val sp = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sp.getString("matricula", "") ?: ""
        val p = sp.getString("password", "") ?: ""

        return if (repository.accesoLogin(m, p).isSuccess) {
            val result = repository.getCargaAcademica()
            result.getOrNull()?.let {
                localRepo.saveCarga(it)
                Result.success()
            } ?: Result.failure()
        } else Result.failure()
    }
}

// --- 3. KARDEX WORKERS ---
class FetchKardexWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = SicenetRepository()
        val sp = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sp.getString("matricula", "") ?: ""
        val p = sp.getString("password", "") ?: ""
        val lineamiento = inputData.getInt("lineamiento", 1)

        return if (repository.accesoLogin(m, p).isSuccess) {
            val result = repository.getKardex(lineamiento)
            if (result.isSuccess) Result.success() else Result.failure()
        } else Result.failure()
    }
}

class StoreKardexWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = SicenetDatabase.getDatabase(applicationContext)
        val localRepo = LocalRepository(db.sicenetDao())
        val repository = SicenetRepository()
        val lineamiento = inputData.getInt("lineamiento", 1)
        val sp = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sp.getString("matricula", "") ?: ""
        val p = sp.getString("password", "") ?: ""

        return if (repository.accesoLogin(m, p).isSuccess) {
            val result = repository.getKardex(lineamiento)
            result.getOrNull()?.let {
                localRepo.saveKardex(it)
                Result.success()
            } ?: Result.failure()
        } else Result.failure()
    }
}

// --- 4. CALIF UNIDADES WORKERS ---
class FetchUnitsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = SicenetRepository()
        val sp = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sp.getString("matricula", "") ?: ""
        val p = sp.getString("password", "") ?: ""

        return if (repository.accesoLogin(m, p).isSuccess) {
            val result = repository.getCalifUnidades()
            if (result.isSuccess) Result.success() else Result.failure()
        } else Result.failure()
    }
}

class StoreUnitsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = SicenetDatabase.getDatabase(applicationContext)
        val localRepo = LocalRepository(db.sicenetDao())
        val repository = SicenetRepository()
        val sp = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sp.getString("matricula", "") ?: ""
        val p = sp.getString("password", "") ?: ""

        return if (repository.accesoLogin(m, p).isSuccess) {
            val result = repository.getCalifUnidades()
            result.getOrNull()?.let {
                localRepo.saveCalifUnidades(it)
                Result.success()
            } ?: Result.failure()
        } else Result.failure()
    }
}

// --- 5. CALIF FINALES WORKERS ---
class FetchFinalsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = SicenetRepository()
        val sp = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sp.getString("matricula", "") ?: ""
        val p = sp.getString("password", "") ?: ""
        val mod = inputData.getInt("mod", 1)

        return if (repository.accesoLogin(m, p).isSuccess) {
            val result = repository.getCalifFinales(mod)
            if (result.isSuccess) Result.success() else Result.failure()
        } else Result.failure()
    }
}

class StoreFinalsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = SicenetDatabase.getDatabase(applicationContext)
        val localRepo = LocalRepository(db.sicenetDao())
        val repository = SicenetRepository()
        val sp = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sp.getString("matricula", "") ?: ""
        val p = sp.getString("password", "") ?: ""
        val mod = inputData.getInt("mod", 1)

        return if (repository.accesoLogin(m, p).isSuccess) {
            val result = repository.getCalifFinales(mod)
            result.getOrNull()?.let {
                localRepo.saveCalifFinales(it)
                Result.success()
            } ?: Result.failure()
        } else Result.failure()
    }
}