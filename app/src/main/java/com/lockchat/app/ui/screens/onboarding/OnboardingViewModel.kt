package com.lockchat.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockchat.app.domain.repository.IdentityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val handle: String       = "",
    val handleError: String? = null,
    val isLoading: Boolean   = false,
    val isComplete: Boolean  = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val identityRepository: IdentityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /** Llamado desde NavGraph para decidir el start destination */
    fun hasIdentity(): Boolean = identityRepository.hasIdentity()

    fun onHandleChange(value: String) {
        // Solo minúsculas, números y guion bajo; máx 32 chars
        val clean = value.lowercase().filter { it.isLetterOrDigit() || it == '_' }.take(32)
        _uiState.update { it.copy(handle = clean, handleError = null) }
    }

    fun onCreateIdentity() {
        val state = _uiState.value
        val err = validateHandle(state.handle)
        if (err != null) {
            _uiState.update { it.copy(handleError = err) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            identityRepository.createIdentity(state.handle)
                .fold(
                    onSuccess = { _uiState.update { it.copy(isLoading = false, isComplete = true) } },
                    onFailure = { e ->
                        _uiState.update { it.copy(isLoading = false, handleError = e.message ?: "Error al crear identidad") }
                    }
                )
        }
    }

    private fun validateHandle(handle: String): String? {
        if (handle.isBlank()) return "El handle no puede estar vacío"
        if (handle.length < 3)  return "Mínimo 3 caracteres"
        if (handle.length > 32) return "Máximo 32 caracteres"
        if (!handle.matches(Regex("[a-z0-9_]+"))) return "Solo letras minúsculas, números y _"
        return null
    }
}
