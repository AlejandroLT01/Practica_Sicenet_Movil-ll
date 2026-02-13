package com.example.practica_sicenet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica_sicenet.data.Alumno
import com.example.practica_sicenet.data.SicenetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SicenetUiState {
    object Idle : SicenetUiState()
    object Loading : SicenetUiState()
    data class Success(val message: String) : SicenetUiState()
    data class ProfileLoaded(val alumno: Alumno) : SicenetUiState()
    data class Error(val message: String) : SicenetUiState()
}

class SicenetViewModel(private val repository: SicenetRepository = SicenetRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<SicenetUiState>(SicenetUiState.Idle)
    val uiState: StateFlow<SicenetUiState> = _uiState

    fun login(matricula: String, contrasenia: String) {
        viewModelScope.launch {
            _uiState.value = SicenetUiState.Loading
            // Establecer la sesión primero
            repository.establishSession()
            // Después, intentar el login
            repository.accesoLogin(matricula, contrasenia).onSuccess { result ->
                if (result.contains("{\"acceso\":true") || result == "1" || result.contains("acceso\":true")) {
                    _uiState.value = SicenetUiState.Success("Login exitoso")
                } else {
                    _uiState.value = SicenetUiState.Error("Error en respuesta del servidor. Verifique credenciales.")
                }
            }.onFailure {
                _uiState.value = SicenetUiState.Error(it.message ?: "Error desconocido")
            }
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            _uiState.value = SicenetUiState.Loading
            repository.getAlumnoAcademicoWithLineamiento().onSuccess { alumno ->
                _uiState.value = SicenetUiState.ProfileLoaded(alumno)
            }.onFailure {
                _uiState.value = SicenetUiState.Error(it.message ?: "Error al obtener perfil")
            }
        }
    }

    fun resetState() {
        _uiState.value = SicenetUiState.Idle
    }
}
