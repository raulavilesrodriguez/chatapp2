package com.packt.settings.ui.edit

import com.packt.domain.user.UserData
import com.packt.settings.domain.usecases.DeleteAccount
import com.packt.settings.domain.usecases.DownloadUrlPhoto
import com.packt.settings.domain.usecases.GetCurrentUserId
import com.packt.settings.domain.usecases.GetHasUser
import com.packt.settings.domain.usecases.GetUser
import com.packt.settings.domain.usecases.SaveUser
import com.packt.settings.domain.usecases.SignOut
import com.packt.settings.domain.usecases.UploadPhoto
import com.packt.ui.ext.normalizeName
import com.packt.ui.navigation.NavRoutes
import com.packt.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val currentUserIdUseCase: GetCurrentUserId,
    private val hasUserUseCase: GetHasUser,
    private val uploadPhoto: UploadPhoto,
    private val downloadPhoto: DownloadUrlPhoto,
    private val deleteAccountUseCase: DeleteAccount,
    private val signOutUseCase: SignOut,
    private val saveUser: SaveUser,
    val getUser: GetUser,
): BaseViewModel() {
    private var _uiState = MutableStateFlow(UserData())
    val uiState: StateFlow<UserData> = _uiState
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init{
        launchCatching {
            if(hasUser){
                _uiState.value = getUser(currentUserId)?: UserData()
            }
        }
    }

    fun reloadUserData() {
        launchCatching {
            if (hasUser) {
                _uiState.value = getUser(currentUserId) ?: UserData()
            }
        }
    }

    val currentUserId
        get() = currentUserIdUseCase()

    val hasUser
        get() = hasUserUseCase()

    fun deleteAccount() {
        launchCatching {
            deleteAccountUseCase()
        }
    }

    fun signOut() {
        signOutUseCase()
    }

    fun updateName(newName: String){
        _uiState.value = _uiState.value.copy(name = newName, nameLowercase = newName.normalizeName())
    }

    fun onName(openScreen: (String) -> Unit){
        launchCatching {
            openScreen(NavRoutes.EditName)
        }
    }

    fun onSaveNameClick(openScreen: (String, String) -> Unit){
        launchCatching {
            val userData = _uiState.value
            saveUser(userData)
            openScreen(NavRoutes.EditUser, NavRoutes.EditName)
        }
    }

    fun onSavePhotoClick(newPhotoUri: String, openScreen: (String) -> Unit){
        launchCatching {
            _isSaving.value = true
            val remotePath = "profile_images/${currentUserId}.jpg"

            // Upload photo to Firebase Storage
            uploadPhoto(newPhotoUri, remotePath)

            // Download URL of the uploaded photo
            val finalPhotoUrl = downloadPhoto(remotePath)
            _uiState.value = _uiState.value.copy(photoUrl = finalPhotoUrl)
            val userData = _uiState.value
            saveUser(userData)

            // navigate to edit screen
            openScreen(NavRoutes.EditUser)
        }.invokeOnCompletion { _isSaving.value = false }
    }

}