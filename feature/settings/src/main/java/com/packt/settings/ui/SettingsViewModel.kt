package com.packt.settings.ui

import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.packt.settings.ui.model.SetUserData
import com.packt.ui.ext.isValidNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

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
        if(newNumber.isValidNumber()){
            uiState.value = uiState.value.copy(number = newNumber)
        } else {

        }
    }



}