package com.example.practica_sicenet.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.practica_sicenet.data.*
import com.example.practica_sicenet.data.local.SicenetDatabase
import com.example.practica_sicenet.data.repository.LocalRepository
import com.example.practica_sicenet.data.repository.SicenetRepository
import com.example.practica_sicenet.data.worker.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SicenetUiState {
    object Idle : SicenetUiState()
    object Loading : SicenetUiState()
    data class Success(val message: String) : SicenetUiState()
    data class Error(val message: String) : SicenetUiState()
}

class SicenetViewModel(application: Application) : AndroidViewModel(application) {
    private val database = SicenetDatabase.getDatabase(application)
    private val localRepository = LocalRepository(database.sicenetDao())
    private val workManager = WorkManager.getInstance(application)

    private val _uiState = MutableStateFlow<SicenetUiState>(SicenetUiState.Idle)
    val uiState: StateFlow<SicenetUiState> = _uiState

    // Flows conectados a Room (siempre activos)
    val alumno = localRepository.getAlumno().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val carga = localRepository.getCarga().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val kardex = localRepository.getKardex().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val califUnidades = localRepository.getCalifUnidades().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val califFinales = localRepository.getCalifFinales().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun login(matricula: String, contrasenia: String) {
        viewModelScope.launch {
            _uiState.value = SicenetUiState.Loading

            // 1. INTENTO OFFLINE REFORZADO
            val sharedPref = getApplication<Application>().getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
            val savedPass = sharedPref.getString("password", "")

            // En lugar de alumno.value, pedimos el primer valor que emita el repositorio
            val alumnoLocal = localRepository.getAlumno().firstOrNull()

            if (alumnoLocal != null && alumnoLocal.matricula == matricula && contrasenia == savedPass) {
                _uiState.value = SicenetUiState.Success("Sesión iniciada (Offline)")
                return@launch
            }

            // 2. INTENTO ONLINE (Si no hay datos locales o son diferentes)
            sharedPref.edit().putString("matricula", matricula).putString("password", contrasenia).apply()

            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val fetchWork = OneTimeWorkRequestBuilder<FetchProfileWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf("matricula" to matricula, "password" to contrasenia))
                .build()

            val storeWork = OneTimeWorkRequestBuilder<StoreProfileWorker>().setConstraints(constraints).build()

            workManager.beginUniqueWork("login_sync", ExistingWorkPolicy.REPLACE, fetchWork)
                .then(storeWork)
                .enqueue()

            // Monitoreamos el fetch para avisar a la UI
            workManager.getWorkInfoByIdLiveData(fetchWork.id).asFlow().collect { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.SUCCEEDED -> _uiState.value = SicenetUiState.Success("Conectado al servidor")
                    WorkInfo.State.FAILED -> _uiState.value = SicenetUiState.Error("Error de red o credenciales")
                    else -> { /* Esperando */ }
                }
            }
        }
    }

    fun syncData(type: String, lineamiento: Int = 1, mod: Int = 1) {
        viewModelScope.launch {
            _uiState.value = SicenetUiState.Loading
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val fetchRequest: OneTimeWorkRequest
            val storeRequest: OneTimeWorkRequest

            // Configuramos los pares de workers según el tipo
            when(type) {
                "CARGA" -> {
                    fetchRequest = OneTimeWorkRequestBuilder<FetchCargaWorker>().setConstraints(constraints).build()
                    storeRequest = OneTimeWorkRequestBuilder<StoreCargaWorker>().setConstraints(constraints).build()
                }
                "KARDEX" -> {
                    val data = workDataOf("lineamiento" to lineamiento)
                    fetchRequest = OneTimeWorkRequestBuilder<FetchKardexWorker>().setConstraints(constraints).setInputData(data).build()
                    storeRequest = OneTimeWorkRequestBuilder<StoreKardexWorker>().setConstraints(constraints).setInputData(data).build()
                }
                "UNIDADES" -> {
                    fetchRequest = OneTimeWorkRequestBuilder<FetchUnitsWorker>().setConstraints(constraints).build()
                    storeRequest = OneTimeWorkRequestBuilder<StoreUnitsWorker>().setConstraints(constraints).build()
                }
                "FINALES" -> {
                    val data = workDataOf("mod" to mod)
                    fetchRequest = OneTimeWorkRequestBuilder<FetchFinalsWorker>().setConstraints(constraints).setInputData(data).build()
                    storeRequest = OneTimeWorkRequestBuilder<StoreFinalsWorker>().setConstraints(constraints).setInputData(data).build()
                }
                else -> return@launch
            }

            workManager.beginUniqueWork("sync_$type", ExistingWorkPolicy.REPLACE, fetchRequest)
                .then(storeRequest)
                .enqueue()

            workManager.getWorkInfoByIdLiveData(fetchRequest.id).asFlow().collect { workInfo ->
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) _uiState.value = SicenetUiState.Idle
                else if (workInfo?.state == WorkInfo.State.FAILED) _uiState.value = SicenetUiState.Error("Sin conexión")
            }
        }
    }


    fun resetState() {
        _uiState.value = SicenetUiState.Idle
    }
}
