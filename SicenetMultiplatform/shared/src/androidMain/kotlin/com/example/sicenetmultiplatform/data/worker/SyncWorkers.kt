package com.example.sicenetmultiplatform.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sicenetmultiplatform.data.SicenetComponent

// --- 1. PROFILE WORKERS ---
class FetchProfileWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncEngine = SicenetComponent.syncEngine
        val matricula = inputData.getString("matricula") ?: ""
        val password = inputData.getString("password") ?: ""

        val result = syncEngine.syncProfile(matricula, password)
        return if (result.isSuccess) Result.success() else Result.failure()
    }
}

class StoreProfileWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncEngine = SicenetComponent.syncEngine
        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val matricula = sharedPref.getString("matricula", "") ?: ""
        val password = sharedPref.getString("password", "") ?: ""

        val result = syncEngine.syncProfile(matricula, password)
        return if (result.isSuccess) Result.success() else Result.failure()
    }
}

// --- 2. CARGA WORKERS ---
class FetchCargaWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncEngine = SicenetComponent.syncEngine
        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sharedPref.getString("matricula", "") ?: ""
        val p = sharedPref.getString("password", "") ?: ""

        val result = syncEngine.syncCarga(m, p)
        return if (result.isSuccess) Result.success() else Result.failure()
    }
}

class StoreCargaWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncEngine = SicenetComponent.syncEngine
        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sharedPref.getString("matricula", "") ?: ""
        val p = sharedPref.getString("password", "") ?: ""

        val result = syncEngine.syncCarga(m, p)
        return if (result.isSuccess) Result.success() else Result.failure()
    }
}

// --- 3. KARDEX WORKERS ---
class FetchKardexWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncEngine = SicenetComponent.syncEngine
        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sharedPref.getString("matricula", "") ?: ""
        val p = sharedPref.getString("password", "") ?: ""
        val lineamiento = inputData.getInt("lineamiento", 1)

        val result = syncEngine.syncKardex(m, p, lineamiento)
        return if (result.isSuccess) Result.success() else Result.failure()
    }
}

class StoreKardexWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncEngine = SicenetComponent.syncEngine
        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sharedPref.getString("matricula", "") ?: ""
        val p = sharedPref.getString("password", "") ?: ""
        val lineamiento = inputData.getInt("lineamiento", 1)

        val result = syncEngine.syncKardex(m, p, lineamiento)
        return if (result.isSuccess) Result.success() else Result.failure()
    }
}

// --- 4. CALIF UNIDADES WORKERS ---
class FetchUnitsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncEngine = SicenetComponent.syncEngine
        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sharedPref.getString("matricula", "") ?: ""
        val p = sharedPref.getString("password", "") ?: ""

        val result = syncEngine.syncUnidades(m, p)
        return if (result.isSuccess) Result.success() else Result.failure()
    }
}

class StoreUnitsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncEngine = SicenetComponent.syncEngine
        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sharedPref.getString("matricula", "") ?: ""
        val p = sharedPref.getString("password", "") ?: ""

        val result = syncEngine.syncUnidades(m, p)
        return if (result.isSuccess) Result.success() else Result.failure()
    }
}

// --- 5. CALIF FINALES WORKERS ---
class FetchFinalsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncEngine = SicenetComponent.syncEngine
        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sharedPref.getString("matricula", "") ?: ""
        val p = sharedPref.getString("password", "") ?: ""
        val mod = inputData.getInt("mod", 1)

        val result = syncEngine.syncFinales(m, p, mod)
        return if (result.isSuccess) Result.success() else Result.failure()
    }
}

class StoreFinalsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncEngine = SicenetComponent.syncEngine
        val sharedPref = applicationContext.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val m = sharedPref.getString("matricula", "") ?: ""
        val p = sharedPref.getString("password", "") ?: ""
        val mod = inputData.getInt("mod", 1)

        val result = syncEngine.syncFinales(m, p, mod)
        return if (result.isSuccess) Result.success() else Result.failure()
    }
}
