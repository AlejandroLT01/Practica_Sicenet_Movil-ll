package com.example.practica_sicenet.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.practica_sicenet.data.*
import com.example.practica_sicenet.data.local.SicenetDatabase
import com.example.practica_sicenet.data.repository.InterfaceSicenet
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
    private val repository: InterfaceSicenet = SicenetRepository()
    private val database = SicenetDatabase.getDatabase(application)
    private val dao = database.sicenetDao()
    private val workManager = WorkManager.getInstance(application)

    private val _uiState = MutableStateFlow<SicenetUiState>(SicenetUiState.Idle)
    val uiState: StateFlow<SicenetUiState> = _uiState

    val alumno: StateFlow<Alumno?> = dao.getAlumno().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val carga: StateFlow<List<CargaAcademica>> = dao.getCarga().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val kardex: StateFlow<List<Kardex>> = dao.getKardex().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val califUnidades: StateFlow<List<CalificacionUnidad>> = dao.getCalifUnidades().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val califFinales: StateFlow<List<CalificacionFinal>> = dao.getCalifFinales().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun login(matricula: String, contrasenia: String) {
        viewModelScope.launch {
            _uiState.value = SicenetUiState.Loading

            // Guardamos credenciales para que los Workers las usen para re-autenticarse
            val sharedPref = getApplication<Application>().getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
            sharedPref.edit()
                .putString("matricula", matricula)
                .putString("password", contrasenia)
                .apply()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWork = OneTimeWorkRequestBuilder<FetchProfileWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf("matricula" to matricula, "password" to contrasenia))
                .build()

            workManager.enqueueUniqueWork("login_sync", ExistingWorkPolicy.REPLACE, syncWork)
            
            workManager.getWorkInfoByIdLiveData(syncWork.id).asFlow().collect { workInfo ->
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

            val workRequest = when(type) {
                "CARGA" -> OneTimeWorkRequestBuilder<FetchCargaWorker>()
                "KARDEX" -> OneTimeWorkRequestBuilder<FetchKardexWorker>().setInputData(workDataOf("lineamiento" to lineamiento))
                "UNIDADES" -> OneTimeWorkRequestBuilder<FetchCalifUnidadesWorker>()
                "FINALES" -> OneTimeWorkRequestBuilder<FetchCalifFinalesWorker>().setInputData(workDataOf("mod" to mod))
                else -> null
            }?.setConstraints(constraints)?.build()

            if (workRequest != null) {
                workManager.enqueueUniqueWork("sync_$type", ExistingWorkPolicy.REPLACE, workRequest)
                workManager.getWorkInfoByIdLiveData(workRequest.id).asFlow().collect { workInfo ->
                    if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                        _uiState.value = SicenetUiState.Idle
                    } else if (workInfo?.state == WorkInfo.State.FAILED) {
                        _uiState.value = SicenetUiState.Error("Fallo sincronización")
                    }
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = SicenetUiState.Idle
    }
}
