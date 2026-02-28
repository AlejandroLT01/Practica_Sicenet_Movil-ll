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

    val alumno: StateFlow<Alumno?> = localRepository.getAlumno().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val carga: StateFlow<List<CargaAcademica>> = localRepository.getCarga().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val kardex: StateFlow<List<Kardex>> = localRepository.getKardex().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val califUnidades: StateFlow<List<CalificacionUnidad>> = localRepository.getCalifUnidades().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val califFinales: StateFlow<List<CalificacionFinal>> = localRepository.getCalifFinales().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun login(matricula: String, contrasenia: String) {
        viewModelScope.launch {
            _uiState.value = SicenetUiState.Loading

            val sharedPref = getApplication<Application>().getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
            sharedPref.edit()
                .putString("matricula", matricula)
                .putString("password", contrasenia)
                .apply()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // REQUISITO: 2 peticiones de trabajo que se deben ver como únicos.
            // El primero consulta (Fetch), el segundo almacena (Store).
            val fetchWork = OneTimeWorkRequestBuilder<FetchProfileWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf("matricula" to matricula, "password" to contrasenia))
                .build()

            val storeWork = OneTimeWorkRequestBuilder<StoreProfileWorker>()
                .setConstraints(constraints)
                .build()

            val continuation = workManager.beginUniqueWork("login_sync", ExistingWorkPolicy.REPLACE, fetchWork)
                .then(storeWork)
            
            continuation.enqueue()
            
            // Monitoreamos el primer worker (o el último) para saber el estatus y mostrar en UI
            workManager.getWorkInfoByIdLiveData(fetchWork.id).asFlow().collect { workInfo ->
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                    _uiState.value = SicenetUiState.Success("Login exitoso")
                } else if (workInfo?.state == WorkInfo.State.FAILED) {
                    _uiState.value = SicenetUiState.Error("Error en autenticación o red")
                }
            }
        }
    }

    fun syncData(type: String, lineamiento: Int = 1, mod: Int = 1) {
        viewModelScope.launch {
            _uiState.value = SicenetUiState.Loading
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val fetchRequest: OneTimeWorkRequest
            val storeRequest: OneTimeWorkRequest

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

            // Encadenamiento de 2 peticiones de trabajo únicas
            workManager.beginUniqueWork("sync_$type", ExistingWorkPolicy.REPLACE, fetchRequest)
                .then(storeRequest)
                .enqueue()

            // Monitoreamos el estatus del primer worker para mostrar info en UI apenas se tenga
            workManager.getWorkInfoByIdLiveData(fetchRequest.id).asFlow().collect { workInfo ->
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                    _uiState.value = SicenetUiState.Idle
                } else if (workInfo?.state == WorkInfo.State.FAILED) {
                    _uiState.value = SicenetUiState.Error("Fallo sincronización")
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = SicenetUiState.Idle
    }
}
