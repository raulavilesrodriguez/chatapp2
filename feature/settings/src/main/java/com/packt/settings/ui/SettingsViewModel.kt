package com.packt.settings.ui

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.PhoneAuthProvider
import com.packt.settings.R
import com.packt.settings.domain.PhoneVerificationResult
import com.packt.settings.domain.usecases.DeleteAccount
import com.packt.settings.domain.usecases.DownloadUrlPhoto
import com.packt.settings.domain.usecases.GetCurrentUserId
import com.packt.settings.domain.usecases.GetHasUser
import com.packt.settings.domain.usecases.ResendVerificationCode
import com.packt.settings.domain.usecases.SignInWithPhoneAuthCredential
import com.packt.settings.domain.usecases.SignInWithVerificationId
import com.packt.settings.domain.usecases.SignOut
import com.packt.settings.domain.usecases.StartPhoneNumberVerification
import com.packt.settings.domain.usecases.UploadPhoto
import com.packt.settings.ui.model.SetUserData
import com.packt.ui.ext.isValidNumber
import com.packt.ui.navigation.NavRoutes
import com.packt.ui.snackbar.SnackbarManager
import com.packt.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import androidx.compose.runtime.State
import com.google.firebase.FirebaseException
import com.packt.domain.user.UserData
import com.packt.settings.domain.usecases.GetPhoneNumber
import com.packt.settings.domain.usecases.GetUser
import com.packt.settings.domain.usecases.SaveUser
import com.packt.settings.ui.model.toUserData
import com.packt.ui.ext.numberFirebaseEcu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val currentUserIdUseCase: GetCurrentUserId,
    private val hasUserUseCase: GetHasUser,
    private val startPhoneNumberVerification: StartPhoneNumberVerification,
    private val signInWithVerificationId: SignInWithVerificationId,
    private val signInWithPhoneAuthCredential: SignInWithPhoneAuthCredential,
    private val resendVerificationCode: ResendVerificationCode,
    private val deleteAccountUseCase: DeleteAccount,
    private val signOutUseCase: SignOut,
    private val getPhoneNumber: GetPhoneNumber,
    private val uploadPhoto: UploadPhoto,
    private val downloadPhoto: DownloadUrlPhoto,
    private val saveUser: SaveUser,
    val getUser: GetUser,
) : BaseViewModel() {

    var uiState = mutableStateOf(SetUserData())
        private set

    private val name
        get() = uiState.value.name
    private val number
        get() = uiState.value.number

    private val _smsCode = MutableStateFlow("")
    val smsCode: StateFlow<String> = _smsCode

    private val _verificationEvents = MutableSharedFlow<PhoneVerificationResult>()
    val verificationEvents: SharedFlow<PhoneVerificationResult> = _verificationEvents.asSharedFlow()

    private val _signInState = mutableStateOf<Boolean?>(null) // true para éxito, false para fallo, null inicial
    val signInState: State<Boolean?> = _signInState

    // Guarda el verificationId y el token para reenvío o inicio de sesión
    private var currentVerificationId: String? = null
    private var currentResendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val _isSavingProfile = mutableStateOf(false)
    val isSavingProfile: State<Boolean> = _isSavingProfile

    private val _userDataStore = MutableStateFlow<UserData?>(UserData())
    val userDataStore: StateFlow<UserData?> = _userDataStore

    fun updateNumber(newNumber: String) {
        uiState.value = uiState.value.copy(number = newNumber)
    }

    fun updateSmsCode(newSmsCode: String) {
        _smsCode.value = newSmsCode
    }

    fun sendVerificationCode(activity: Activity) {
        /**
        if(!number.isValidNumber()){
            Log.d("SettingsViewModel", "Number ES INVALIDO. Current number: '${number}'. Setting to empty.")
            SnackbarManager.showMessage(R.string.number_error)
            //uiState.value = uiState.value.copy(number = "")
            Log.d("SettingsViewModel", "Number ES INVALIDO. Current number: '${number}' y ${uiState.value.number}. Setting to empty.")
            return
        } */

        val phoneNumber = number.numberFirebaseEcu()
        Log.d("SettingsViewModel", "Number ES VALIDO. Current number: '${phoneNumber}'. LOCO.")

        launchCatching {
            startPhoneNumberVerification(phoneNumber, activity)
                .collect { result ->
                    when (result) {
                        is PhoneVerificationResult.CodeSent -> {
                            currentVerificationId = result.verificationId
                            currentResendToken = result.token
                        }
                        is PhoneVerificationResult.VerificationCompleted -> {
                            // Intenta iniciar sesión automáticamente
                            val success = signInWithPhoneAuthCredential(result.credential)
                            _signInState.value = success
                        }
                        is PhoneVerificationResult.VerificationFailed -> {
                            // No es necesario actualizar signInState aquí,
                            Log.w("SettingsViewModel", "VerificationFailed: ${result.exception.message}", result.exception)
                        }
                    }
                    _verificationEvents.emit(result) // Emite todos los eventos para que la UI reaccione
                }
        }
    }

    fun submitSmsCode() {
        val verificationId = currentVerificationId
        if (verificationId == null) {
            launchCatching {
                // Emite un evento de error personalizado o maneja este estado
                _verificationEvents.emit(PhoneVerificationResult.VerificationFailed(
                    FirebaseException("Verification ID not found.")
                ))
            }
            return
        }
        launchCatching {
            val success = signInWithVerificationId(verificationId, _smsCode.value)
            _signInState.value = success
            if (!success) {
                // Emite un evento de error si el inicio de sesión falla
                _verificationEvents.emit(PhoneVerificationResult.VerificationFailed(
                    FirebaseException("Invalid SMS code.")
                ))
            }
        }
    }

    fun resendCode(activity: Activity) {
        val token = currentResendToken
        if (token == null) {
            launchCatching {
                _verificationEvents.emit(PhoneVerificationResult.VerificationFailed(FirebaseException("Resend token not found.")))
            }
            return
        }
        launchCatching {
            // Similar a sendVerificationCode, recolecta y actualiza los IDs/tokens
            resendVerificationCode(number, activity, token)
                .collect { result ->
                    if (result is PhoneVerificationResult.CodeSent) {
                        currentVerificationId = result.verificationId
                        currentResendToken = result.token
                    } else if (result is PhoneVerificationResult.VerificationCompleted) {
                        val success = signInWithPhoneAuthCredential(result.credential)
                        _signInState.value = success
                    }
                    _verificationEvents.emit(result)
                }
        }
    }

    fun resetSignInState() {
        _signInState.value = null
    }

    val currentUserId
        get() = currentUserIdUseCase()

    val hasUser
        get() = hasUserUseCase()

    fun deleteAccount() {
        launchCatching {
            deleteAccountUseCase()
            _signInState.value = null
        }
    }

    fun signOut() {
        signOutUseCase()
    }

    fun loginSuccess(openAndPopUp: (String, String) -> Unit) {
        launchCatching {
            openAndPopUp(NavRoutes.Settings, NavRoutes.Login)
        }
    }

    fun alreadyLoggedIn(openAndPopUp: (String, String) -> Unit) {
        launchCatching {
            if (hasUser) {
                val user = getUser(currentUserId)
                if (user?.name != null) {
                    openAndPopUp(NavRoutes.ConversationsList, NavRoutes.Splash)
                } else {
                    openAndPopUp(NavRoutes.Settings, NavRoutes.Splash)
                }
            } else {
                openAndPopUp(NavRoutes.Login, NavRoutes.Splash)
            }
        }
    }

    fun getUserData() {
        launchCatching {
            if(hasUser) {
                _userDataStore.value = getUser(currentUserId)
            }
        }
    }

    fun updatePhotoUri(newPhotoUri: String) {
        uiState.value = uiState.value.copy(photoUri = newPhotoUri)
    }

    fun updateName(newName: String) {
        uiState.value = uiState.value.copy(name = newName)
    }

    fun onSettingClick(openAndPopUp: (String, String) -> Unit) {
        if (name.isBlank()) {
            SnackbarManager.showMessage(R.string.empty_fields)
            return
        }

        _isSavingProfile.value = true

        launchCatching {

            var finalPhotoUrl = uiState.value.photoUri

            // To upload local Uri to Firebase Storage. Firestorage don't know remote URLs
            // such as DEFAULT_AVATAR_URL
            if(finalPhotoUrl.startsWith("content://") ||
                finalPhotoUrl.startsWith("android.resource://") ||
                finalPhotoUrl.startsWith("file://")){

                val remotePath = "profile_images/${currentUserId}.jpg"

                // Upload photo to Firebase Storage
                uploadPhoto(uiState.value.photoUri, remotePath)

                // Download URL of the uploaded photo
                finalPhotoUrl = downloadPhoto(remotePath)
            }

            // Update user data with the download URL
            uiState.value = uiState.value.copy(photoUri = finalPhotoUrl)

            // Update phone number
            val phoneNumber = getPhoneNumber()
            uiState.value = uiState.value.copy(number = phoneNumber ?: "")

            // prepare to upload user data to fire store
            val userData = uiState.value.toUserData(currentUserId)
            saveUser(userData)

            // Navigate to ConversationsList screen
            openAndPopUp(NavRoutes.ConversationsList, NavRoutes.Settings)
        }.invokeOnCompletion { _isSavingProfile.value = false }
    }
}