package com.example.sicenetmultiplatform.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sicenetmultiplatform.data.*
import com.example.sicenetmultiplatform.data.repository.LocalRepository
import com.example.sicenetmultiplatform.data.repository.SicenetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SicenetUiState {
    object Idle : SicenetUiState()
    object Loading : SicenetUiState()
    data class Success(val message: String) : SicenetUiState()
    data class Error(val message: String) : SicenetUiState()
}

class SicenetViewModel(
    private val localRepository: LocalRepository,
    private val sicenetRepository: SicenetRepository
) : ViewModel() {
    
    // For now, these must be initialized or injected. 
    // This is a simplified version for KMP migration.
    private val syncEngine = SicenetSyncEngine(localRepository, sicenetRepository)

    private val _uiState = MutableStateFlow<SicenetUiState>(SicenetUiState.Idle)
    val uiState: StateFlow<SicenetUiState> = _uiState

    val alumno = localRepository.getAlumno().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val carga = localRepository.getCarga().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val kardex = localRepository.getKardex().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val califUnidades = localRepository.getCalifUnidades().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val califFinales = localRepository.getCalifFinales().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Temporary storage for credentials (in a real app, use MultiplatformSettings)
    private var savedMatricula: String = ""
    private var savedPassword: String = ""

    fun login(matricula: String, contrasenia: String) {
        viewModelScope.launch {
            _uiState.value = SicenetUiState.Loading
            
            val result = syncEngine.syncProfile(matricula, contrasenia)
            if (result.isSuccess) {
                savedMatricula = matricula
                savedPassword = contrasenia
                _uiState.value = SicenetUiState.Success("Conectado al servidor")
            } else {
                _uiState.value = SicenetUiState.Error("Error de red o credenciales")
            }
        }
    }

    fun syncData(type: String, lineamiento: Int = 1, mod: Int = 1) {
        viewModelScope.launch {
            _uiState.value = SicenetUiState.Loading
            
            val result = when(type) {
                "CARGA" -> syncEngine.syncCarga(savedMatricula, savedPassword)
                "KARDEX" -> syncEngine.syncKardex(savedMatricula, savedPassword, lineamiento)
                "UNIDADES" -> syncEngine.syncUnidades(savedMatricula, savedPassword)
                "FINALES" -> syncEngine.syncFinales(savedMatricula, savedPassword, mod)
                else -> Result.failure(Exception("Unknown type"))
            }

            if (result.isSuccess) {
                _uiState.value = SicenetUiState.Idle
            } else {
                _uiState.value = SicenetUiState.Error("Error al sincronizar")
            }
        }
    }

    fun resetState() {
        _uiState.value = SicenetUiState.Idle
    }
}
