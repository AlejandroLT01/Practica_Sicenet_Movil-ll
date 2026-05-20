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
    
    private val syncEngine = SicenetSyncEngine(localRepository, sicenetRepository)

    private val _uiState = MutableStateFlow<SicenetUiState>(SicenetUiState.Idle)
    val uiState: StateFlow<SicenetUiState> = _uiState

    // Observamos los datos directamente de la base de datos local
    val alumno = localRepository.getAlumno().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val carga = localRepository.getCarga().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val kardex = localRepository.getKardex().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val califUnidades = localRepository.getCalifUnidades().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val califFinales = localRepository.getCalifFinales().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var savedMatricula: String = ""
    private var savedPassword: String = ""

    fun login(matricula: String, contrasenia: String) {
        viewModelScope.launch {
            _uiState.value = SicenetUiState.Loading
            
            try {
                // 1. Intentamos sincronizar con el servidor
                val result = syncEngine.syncProfile(matricula, contrasenia)
                
                if (result.isSuccess) {
                    savedMatricula = matricula
                    savedPassword = contrasenia
                    _uiState.value = SicenetUiState.Success("Conectado al servidor")
                } else {
                    // 2. Si falla la red, intentamos login offline
                    handleOfflineLogin(matricula, contrasenia)
                }
            } catch (e: Exception) {
                handleOfflineLogin(matricula, contrasenia)
            }
        }
    }

    private suspend fun handleOfflineLogin(matricula: String, contrasenia: String) {
        // Buscamos si el alumno ya existe en la DB local
        val localAlumno = localRepository.getAlumno().firstOrNull()
        
        if (localAlumno != null && localAlumno.matricula == matricula) {
            savedMatricula = matricula
            savedPassword = contrasenia
            _uiState.value = SicenetUiState.Success("Modo Offline: Cargando datos locales")
        } else {
            _uiState.value = SicenetUiState.Error("Sin conexión y no hay datos locales guardados")
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
                // Si falla la sincronización de una sección, solo avisamos pero mantenemos los datos locales
                _uiState.value = SicenetUiState.Error("No se pudo actualizar. Mostrando datos locales.")
                // Después de unos segundos volvemos a Idle para que el error no se quede pegado
                kotlinx.coroutines.delay(3000)
                _uiState.value = SicenetUiState.Idle
            }
        }
    }

    fun resetState() {
        _uiState.value = SicenetUiState.Idle
    }
}
