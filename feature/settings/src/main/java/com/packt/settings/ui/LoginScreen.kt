package com.packt.settings.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.settings.R
import com.packt.settings.domain.PhoneVerificationResult
import com.packt.settings.ui.model.SetUserData
import com.packt.ui.composables.BasicBottomBar
import com.packt.ui.composables.BasicToolbar
import com.packt.ui.composables.DialogCancelButton
import com.packt.ui.composables.DialogConfirmButton
import com.packt.ui.ext.isValidNumber
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun LoginScreen(
    openAndPopUp: (String, String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState
    val smsCode by viewModel.smsCode.collectAsState()

    val context = LocalContext.current
    val activity = context as Activity

    LaunchedEffect(viewModel.hasUser) {
        if (viewModel.hasUser){
            viewModel.alreadyLoggedIn(openAndPopUp)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            BasicToolbar(R.string.settings_1)
        },
        bottomBar = {
            BasicBottomBar(R.string.made_by)
        },
        content = { innerPadding ->
            LoginScreenContent(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState,
                updateNumber = viewModel::updateNumber,
                sendVerificationCode = {viewModel.sendVerificationCode(activity)},
                smsCode = smsCode,
                updateSmsCode = viewModel::updateSmsCode,
                submitSmsCode = viewModel::submitSmsCode,
                signInState = viewModel.signInState.value,
                loginSuccess = {viewModel.loginSuccess(openAndPopUp)},
                resendVerificationCode = {viewModel.resendCode(activity)},
                verificationEventsFlow = viewModel.verificationEvents,
                resetSignInState = viewModel::resetSignInState
            )
        }
    )
}

@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    uiState: SetUserData,
    updateNumber: (String) -> Unit,
    sendVerificationCode: () -> Unit,
    smsCode: String,
    updateSmsCode: (String) -> Unit,
    submitSmsCode: () -> Unit,
    signInState: Boolean? = null,
    loginSuccess: () -> Unit,
    resendVerificationCode: () -> Unit,
    verificationEventsFlow: SharedFlow<PhoneVerificationResult>,
    resetSignInState: () -> Unit
){
    var showDialogSMS by remember { mutableStateOf(false) }
    var showCircularProgress by remember { mutableStateOf(false) }
    var showResendOption by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(30) }

    val context = LocalContext.current

    LaunchedEffect(verificationEventsFlow) {
        verificationEventsFlow.collect { result ->
            if (result is PhoneVerificationResult.VerificationCompleted) {
                if (showCircularProgress){
                    showCircularProgress = false
                    if (signInState != true){
                        showResendOption = true
                        Toast.makeText(
                            context,
                            "Error de verificación. Intenta de nuevo",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if(result is PhoneVerificationResult.VerificationFailed){
                if (showCircularProgress){
                    showCircularProgress = false
                    showResendOption = true
                } else if(showDialogSMS){
                    showDialogSMS = false
                }
                Toast.makeText(
                    context,
                    "Error de verificación. Intenta de nuevo, verifica tu número",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(showCircularProgress) {
        if(showCircularProgress){
            showResendOption = false
            timerSeconds = 30
            while (timerSeconds > 0 && showCircularProgress) {
                delay(1000)
                timerSeconds--
            }
            if(showCircularProgress && timerSeconds == 0){
                showCircularProgress = false
                showResendOption = true
                Toast.makeText(context, "Tiempo de espera agotado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(signInState) {
        if(signInState == true) {
            loginSuccess()
            showCircularProgress = false
            showDialogSMS = false
            showResendOption = false
        } else if (signInState == false && showCircularProgress){
            showCircularProgress = false
            showResendOption = true
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            stringResource(R.string.app_name),
            color = Color.Green,
            fontSize = 40.sp,
            modifier = Modifier.padding(8.dp)
        )

        when{
            showCircularProgress -> {
                ShowProgress(timerSeconds)
            }
            showResendOption -> {
                ReSendSmsCode(
                    context = context,
                    showResenOption = {showResendOption = it},
                    showCircularProgress = {showCircularProgress = it},
                    showDialogSMS = {showDialogSMS = it},
                    resendVerificationCode = resendVerificationCode,
                    updateNumber = updateNumber,
                    resetSignInState = resetSignInState
                )
            }
            showDialogSMS -> {
                SendSmsCode(
                    uiState = uiState,
                    smsCode = smsCode,
                    updateSmsCode = updateSmsCode,
                    submitSmsCode = {
                        submitSmsCode()
                        showCircularProgress = true
                        showResendOption = false
                    },
                    showDialogSMS = {showDialogSMS = it},
                    updateNumber = updateNumber,
                    resetSignInState = resetSignInState
                )
            }
            else -> {
                RegisterNumber(
                    uiState = uiState,
                    updateNumber = updateNumber,
                    sendVerificationCode = sendVerificationCode,
                    showDialogSMS = {showDialogSMS = it},
                    context = context
                )
            }
        }

    }

}


@Composable
fun RegisterNumber(
    uiState: SetUserData,
    updateNumber: (String) -> Unit,
    sendVerificationCode: () -> Unit,
    showDialogSMS: (Boolean) -> Unit,
    context: Context
){
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.register_number),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            modifier = Modifier.padding(8.dp)
        )

        OutlinedTextField(
            value = uiState.number,
            onValueChange = {updateNumber(it)},
            label = {Text(stringResource(R.string.user_number))},
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Green,
                unfocusedLabelColor = Color.Green
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if(uiState.number.isValidNumber()){
                        sendVerificationCode()
                        showDialogSMS(true)
                    }
                    else {
                        showDialogSMS(false)
                        updateNumber("")
                        /**
                        Toast.makeText(
                            context,
                            "Error, número móvil incorrecto",
                            Toast.LENGTH_SHORT
                        ).show() */
                    }
                }
            ) {
                Text(stringResource(R.string.send))
            }
            OutlinedButton(
                onClick = {
                    (context as? Activity)?.finishAffinity()
                          },
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}

@Composable
fun SendSmsCode(
    uiState: SetUserData,
    smsCode: String,
    updateSmsCode: (String) -> Unit,
    submitSmsCode: () -> Unit,
    showDialogSMS: (Boolean) -> Unit,
    updateNumber: (String) -> Unit,
    resetSignInState: () -> Unit
){
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.send_sms_text, uiState.number),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            modifier = Modifier.padding(8.dp)
        )

        OutlinedTextField(
            value = smsCode,
            onValueChange = { updateSmsCode(it) },
            label = {Text(stringResource(R.string.send_sms_title))},
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Green,
                unfocusedLabelColor = Color.Green
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DialogConfirmButton(R.string.send) {
                submitSmsCode()
            }
            DialogCancelButton(R.string.cancel) {
                showDialogSMS(false)
                updateNumber("")
                resetSignInState()
            }
        }
    }
}

@Composable
fun ReSendSmsCode(
    context: Context,
    showResenOption: (Boolean) -> Unit,
    showCircularProgress: (Boolean) -> Unit,
    showDialogSMS: (Boolean) -> Unit,
    resendVerificationCode: () -> Unit,
    updateNumber: (String) -> Unit,
    resetSignInState: () -> Unit
){
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.resend_option),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    showResenOption(false)
                    showCircularProgress(true)
                    resendVerificationCode()
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(stringResource(R.string.resend_sms))
            }
            OutlinedButton(
                onClick = {
                    showResenOption(false)
                    showDialogSMS(false)
                    updateNumber("")
                    resetSignInState()
                          },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}

@Composable
fun ShowProgress(
    timerSeconds: Int
){
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
        Text(
            text = "Verificando... ${if (timerSeconds > 0) "$timerSeconds s" else ""}",
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}