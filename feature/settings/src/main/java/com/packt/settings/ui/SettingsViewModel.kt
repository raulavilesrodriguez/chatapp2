package com.packt.settings.ui

import androidx.compose.runtime.mutableStateOf
import com.packt.settings.R
import com.packt.settings.ui.model.SetUserData
import com.packt.ui.ext.isOnlyNumbers
import com.packt.ui.ext.isValidNumber
import com.packt.ui.snackbar.SnackbarManager
import com.packt.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : BaseViewModel() {

    var uiState = mutableStateOf(SetUserData())
        private set

    private val name
        get() = uiState.value.name
    private val number
        get() = uiState.value.number


    fun updatePhotoUri(newPhotoUri: String) {
        uiState.value = uiState.value.copy(photoUri = newPhotoUri)
    }

    fun updateName(newName: String) {
        uiState.value = uiState.value.copy(name = newName)
    }

    fun updateNumber(newNumber: String) {
        if(newNumber.isOnlyNumbers()){
            uiState.value = uiState.value.copy(number = newNumber)
        } else {
            SnackbarManager.showMessage(R.string.number_error)
        }
    }

    fun onSettingClick() {
        if (name.isBlank() || number.isBlank()) {
            SnackbarManager.showMessage(R.string.empty_fields)
            return
        }

        if (number.isValidNumber()) {
            SnackbarManager.showMessage(R.string.number_error)
            return
        }


    }


}