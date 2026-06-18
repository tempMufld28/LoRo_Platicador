package com.lockchat.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockchat.app.domain.model.Contact
import com.lockchat.app.domain.model.Identity
import com.lockchat.app.domain.repository.ContactRepository
import com.lockchat.app.domain.repository.IdentityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.lockchat.app.data.local.ThemePreferences
import javax.inject.Inject

data class ProfileUiState(
    val handle: String          = "",
    val nodeIdFormatted: String = "",
    val qrData: String          = "",      // JSON del QR propio
    val contacts: List<Contact> = emptyList(),
    val isEditingHandle: Boolean = false,
    val editHandleText: String  = "",
    val editHandleError: String? = null,
    val showWarningDialog: Boolean = false,
    val isLoading: Boolean      = false,
    val error: String?          = null
) {
    val initials: String get() = handle.take(2).uppercase()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val identityRepository: IdentityRepository,
    private val contactRepository: ContactRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _isEditingHandle      = MutableStateFlow(false)
    private val _editHandleText       = MutableStateFlow("")
    private val _editHandleError      = MutableStateFlow<String?>(null)
    private val _showWarningDialog    = MutableStateFlow(false)
    private val _isLoading            = MutableStateFlow(false)

    val isDarkMode: StateFlow<Boolean> = themePreferences.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    val uiState: StateFlow<ProfileUiState> = combine(
        identityRepository.observeIdentity(),
        contactRepository.observeAll(),
        _isEditingHandle,
        _editHandleText,
        _editHandleError,
        _showWarningDialog
    ) { array ->
        val identity = array[0] as Identity?
        @Suppress("UNCHECKED_CAST")
        val contacts = array[1] as List<Contact>
        val editing = array[2] as Boolean
        val editText = array[3] as String
        val editError = array[4] as String?
        val showWarning = array[5] as Boolean

        ProfileUiState(
            handle          = identity?.handle ?: "",
            nodeIdFormatted = identity?.nodeIdFormatted ?: "",
            qrData          = identity?.let { buildQrJson(it.nodeId, it.handle) } ?: "",
            contacts        = contacts,
            isEditingHandle = editing,
            editHandleText  = editText,
            editHandleError = editError,
            showWarningDialog = showWarning
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState()
    )

    fun onEditHandle() {
        _editHandleText.value = uiState.value.handle
        _isEditingHandle.value = true
    }

    fun onHandleTextChange(text: String) {
        val clean = text.lowercase().filter { it.isLetterOrDigit() || it == '_' }.take(10)
        _editHandleText.value = clean
        _editHandleError.value = null
    }

    fun onThemeToggle(isDark: Boolean) {
        viewModelScope.launch {
            themePreferences.setDarkMode(isDark)
        }
    }

    fun onHandleSave() {
        val newHandle = _editHandleText.value.trim()
        val oldHandle = uiState.value.handle

        if (newHandle == oldHandle) {
            _isEditingHandle.value = false
            return
        }

        if (newHandle.length != 10) {
            _editHandleError.value = "Debe tener exactamente 10 caracteres"
            return
        }

        if (oldHandle.isNotBlank()) {
            // Mostrar advertencia si ya tenía un handle configurado
            _showWarningDialog.value = true
        } else {
            executeSaveHandle(newHandle)
        }
    }

    fun onConfirmHandleChange() {
        _showWarningDialog.value = false
        val newHandle = _editHandleText.value.trim()
        executeSaveHandle(newHandle)
    }

    fun onCancelHandleChange() {
        _showWarningDialog.value = false
    }

    private fun executeSaveHandle(newHandle: String) {
        viewModelScope.launch {
            _isLoading.value = true
            identityRepository.updateHandle(newHandle)
                .fold(
                    onSuccess = {
                        android.util.Log.i("ProfileViewModel", "Handle actualizado exitosamente a: $newHandle")
                        _editHandleError.value = null
                    },
                    onFailure = { e ->
                        android.util.Log.e("ProfileViewModel", "Error al actualizar handle", e)
                        _editHandleError.value = e.message ?: "Error al guardar handle"
                    }
                )
            _isEditingHandle.value = false
            _isLoading.value = false
        }
    }

    fun onDismissEditHandle() {
        _isEditingHandle.value = false
        _editHandleError.value = null
    }

    fun onDeleteContact(nodeId: String) {
        viewModelScope.launch {
            contactRepository.deleteContact(nodeId)
        }
    }

    private fun buildQrJson(nodeId: String, handle: String): String =
        JSONObject()
            .put("v", 1)
            .put("nodeId", nodeId)
            .put("handle", handle)
            .put("transport", "BLE")
            .toString()
}
