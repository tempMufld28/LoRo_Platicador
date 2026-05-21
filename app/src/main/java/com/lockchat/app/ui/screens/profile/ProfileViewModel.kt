package com.lockchat.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockchat.app.domain.model.Contact
import com.lockchat.app.domain.repository.ContactRepository
import com.lockchat.app.domain.repository.IdentityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class ProfileUiState(
    val handle: String          = "",
    val nodeIdFormatted: String = "",
    val qrData: String          = "",      // JSON del QR propio
    val contacts: List<Contact> = emptyList(),
    val isEditingHandle: Boolean = false,
    val editHandleText: String  = "",
    val editHandleError: String? = null,
    val isLoading: Boolean      = false,
    val error: String?          = null
) {
    val initials: String get() = handle.take(2).uppercase()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val identityRepository: IdentityRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _isEditingHandle    = MutableStateFlow(false)
    private val _editHandleText     = MutableStateFlow("")
    private val _editHandleError    = MutableStateFlow<String?>(null)
    private val _isLoading          = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> = combine(
        identityRepository.observeIdentity(),
        contactRepository.observeAll(),
        _isEditingHandle,
        _editHandleText,
        _editHandleError
    ) { identity, contacts, editing, editText, editError ->
        ProfileUiState(
            handle          = identity?.handle ?: "",
            nodeIdFormatted = identity?.nodeIdFormatted ?: "",
            qrData          = identity?.let { buildQrJson(it.nodeId, it.handle) } ?: "",
            contacts        = contacts,
            isEditingHandle = editing,
            editHandleText  = editText,
            editHandleError = editError
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
        val clean = text.lowercase().filter { it.isLetterOrDigit() || it == '_' }.take(32)
        _editHandleText.value = clean
        _editHandleError.value = null
    }

    fun onHandleSave() {
        val newHandle = _editHandleText.value.trim()
        if (newHandle.length < 3) {
            _editHandleError.value = "Mínimo 3 caracteres"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            identityRepository.updateHandle(newHandle)
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
